/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.meatweapons.meatgun.module;

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.ParamCodecs;
import com.neep.meatlib.graphics.GraphicsEffects;
import com.neep.meatlib.network.GlobalChannelManager;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.item.meatgun.MeatgunModuleItem;
import com.neep.meatweapons.meatgun.AmmunitionType;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import com.neep.meatweapons.network.MeatgunNetwork;
import com.neep.meatweapons.particle.MWGraphicsEffects;
import com.neep.neepmeat.machine.phage_ray.PhageRayEntity;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.PlayerInventoryStorage;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ShortPhageRayModule extends AmmunitionUsingModule
{
    public static final GlobalChannelManager<SetBreakProgress> BREAK_PROGRESS_S2C = GlobalChannelManager.create(
            ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "break_progress"),
            ChannelFormat.builder(SetBreakProgress.class).param(ParamCodec.BOOLEAN).param(ParamCodecs.BLOCK_POS).param(ParamCodec.FLOAT).build());

//    public static final GlobalChannelManager<Consumer<BlockPos>> ON_BREAK_S2C = GlobalChannelManager.create(
//            Identifier.of(MeatWeapons.NAMESPACE, "phage_ray/break_block"),
//            ChannelFormat.builder(ParamCodecs.<Consumer<BlockPos>>as(Consumer.class)).param(ParamCodecs.BLOCK_POS).build());

    public static final double DISTANCE = 15;

    protected final int maxCooldown;

    @Nullable protected BlockPos target;

    protected float progress;
    // Store maximum break progress instead of scaling to [0 1]. Unbreakable if negative.
    // Fixes issues when retargeting from a soft block to a hard block.
    protected float maxProgress;

    protected int blocksRemaining = 0; // Number of blocks that can be broken before power/ammo consumption

    protected int cooldown = 0;
    protected int counter;
    private int heldTrigger = -1;

//    public int timer = 0;
//    public long savedTime = 0;

    protected ListTag enchantments = new ListTag();

    // Client only
    public float lerpPanelOffset;

    public ShortPhageRayModule(RootModuleHolder.Listener l, CompoundTag nbt)
    {
        this(l);
        readNbt(nbt, null);
    }

    public ShortPhageRayModule(RootModuleHolder.Listener l, ItemStack stack)
    {
        this(l);
        this.enchantments = stack.getEnchantmentTags().copy();
    }

    protected ShortPhageRayModule(RootModuleHolder.Listener listener)
    {
        super(listener, AmmunitionType.ENERGY);
        this.maxCooldown = (int) getType().attributes().get(Attribute.COOLDOWN);
    }

    public boolean canHarvest(BlockState state)
    {
        return true;
    }

    public float getMaxProgress(Level world, BlockPos pos, BlockState state)
    {
        // Refer to PhageRayEntity$calcBlockBreakingDelta
        float f = state.getDestroySpeed(world, pos);
        return f * 30;
    }

    @Override
    public void tickTrigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        // Try consuming energy
        boolean reloaded = false;
        if (blocksRemaining <= 0 && cooldown == 0)
        {
            if (consume(player.getInventory(), player, hand))
            {
                cooldown = 0;
                blocksRemaining = 8;
                reloaded = true;
            }
        }

        if (blocksRemaining > 0)
        {
            if (counter % 5 == 0)
            {
                MeatgunNetwork.sendShake(player, 0.006f, 10, hand);
            }

            if (counter % 30 == 0 || reloaded)
            {
                // Not going to bother sending this to every player as the effect needs to do a raycast every frame (eek)
                syncBeamEffect((ServerPlayer) player, world, Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, 0.2f, 30 - counter % 30, hand);
            }
        }

        if (blocksRemaining > 0)
        {
            HitResult hit = player.pick(DISTANCE, 0, false);

            BlockPos hitPos = null;
            if (hit instanceof BlockHitResult blockHit && !world.getBlockState(blockHit.getBlockPos()).isAir())
            {
                hitPos = blockHit.getBlockPos().immutable();
            }

            if (!Objects.equals(hitPos, target))
            {
                // There's an issue here
                // Communication introduces latency between the client player's rotation and the server player's rotation
                // If progress is set to 0 here, initial blocks (and retargeted blocks) take longer to break than successive ones
                // However, this gives the side effect of being able to half-break one block, retarget to a block with higher hardness and retain the break progress.

                if (target == null)
                {
                    progress = 0;
                }

                target = hitPos;
                if (target != null)
                    maxProgress = getMaxProgress(world, target, world.getBlockState(target));
                else
                    maxProgress = -1;
            }
        }

        if (blocksRemaining > 0 && target != null)
        {
            if (maxProgress >= 0)
            {
                if (progress < maxProgress)
                {
                    float speed = 5
                            + 1.1f * EnchantmentHelper.deserializeEnchantments(enchantments).getOrDefault(Enchantments.BLOCK_EFFICIENCY, 0)
                            + 1.5f * listener.getHolder().moduleCache().modulesOfType(MeatgunModules.PHAGE_RAY_SPEED_MODIFIER).size();
                    float newProgress = progress + speed;

                    if (newProgress != progress)
                    {
                        progress = newProgress;
                        world.destroyBlockProgress(player.getId(), target, (int) (progress / maxProgress * 10));
                        BREAK_PROGRESS_S2C.emitter(player).setBreakProgress(false, target, progress / maxProgress);
                    }

                }

                if (progress >= maxProgress)
                {
                    // TODO: add helper modules that give these properties
                    boolean harvest = true;
                    boolean retrieve = false;

                    BlockEntity blockEntity = world.getBlockEntity(target);
                    BlockState state = world.getBlockState(target);
                    Block block = state.getBlock();

                    block.playerWillDestroy(world, target, state, player);

                    boolean removed = world.removeBlock(target, false);
                    world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, target, Block.getId(state));

                    if (removed)
                        block.destroy(world, target, state);

                    boolean canHarvest = harvest && canHarvest(state);
                    if (removed && canHarvest)
                    {
                        if (retrieve)
                        {
                            List<ItemStack> dropped = Block.getDrops(state,
                                    (ServerLevel) world,
                                    target,
                                    world.getBlockEntity(target),
                                    player,
                                    stack
                            );

                            boolean inserted = insertItems(player, dropped);

                            if (!inserted)
                                Block.dropResources(state, world, target, blockEntity, player, stack);

                        }
                        else
                        {
                            // Causes exhaustion
//                        block.afterBreak(world, player, target, state, world.getBlockEntity(target), itemStack2);

                            player.awardStat(Stats.BLOCK_MINED.get(block));
                            Block.dropResources(state, world, target, blockEntity, player, stack);
                        }
                    }

                    progress = 0;
                    blocksRemaining--;
                    target = null;
                }
            }


        }

        counter++;
    }

    @Override
    public void trigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        if (heldTrigger == -1)
        {
            heldTrigger = id;
            listener.markDirty(RootModuleHolder.Reason.SAVE_DATA);
            MeatgunNetwork.SEND_ANIMATION.emitter(player).apply("phage_ray_hold", hand, null);
        }
    }

    @Override
    public void release(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        if (heldTrigger == id)
        {
            counter = 0;
            BREAK_PROGRESS_S2C.emitter(player).setBreakProgress(true, BlockPos.ZERO, -1);

            target = null;
            heldTrigger = -1;

            listener.markDirty(RootModuleHolder.Reason.SAVE_DATA);
        }
    }

    protected boolean insertItems(Player player, List<ItemStack> stacks)
    {
        try (Transaction transaction = Transaction.openOuter())
        {
            PlayerInventoryStorage storage = PlayerInventoryStorage.of(player);
            for (var itemStack : stacks)
            {
                long count = itemStack.getCount();
                long inserted = storage.insert(ItemVariant.of(itemStack), itemStack.getCount(), transaction);

                if (inserted != count)
                {
                    transaction.abort();
                    return false;
                }
            }

            transaction.commit();
            return true;
        }
    }

    @Override
    public void tick(Player player)
    {
        cooldown = Math.max(0, cooldown - 1);
    }

    @Override
    public Type<? extends MeatgunModule> getType()
    {
        return MeatgunModules.SHORT_PHAGE_RAY;
    }

    @Override
    public ItemStack createStack(MeatgunModuleItem item)
    {
        ItemStack stack = super.createStack(item);
        stack.getOrCreateTag().put("Enchantments", enchantments);
        return stack;
    }

    private void syncBeamEffect(ServerPlayer player, Level world, Vec3 start, Vec3 end, Vec3 velocity, float scale, int maxTime, InteractionHand hand)
    {
        if (world.isClientSide)
            throw new IllegalStateException("packet create called on the client!");

        FriendlyByteBuf byteBuf = PacketByteBufs.create();

        // Important things
        byteBuf.writeBoolean(hand == InteractionHand.OFF_HAND);
        byteBuf.writeVarInt(player.getId());
        byteBuf.writeUUID(uuid);

        GraphicsEffects.CHANNEL.emitter(player).apply(world.dimension(), MWGraphicsEffects.SMALL_PHAGE_RAY, start, end, velocity, scale, maxTime, byteBuf);
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        super.writeNbt(nbt);
        nbt.putInt("held_trigger", heldTrigger);
        nbt.putInt("blocks_remaining", blocksRemaining);
        nbt.put("enchantments", enchantments);
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt, @Nullable Matrix4f global)
    {
        super.readNbt(nbt, global);
        this.heldTrigger = nbt.getInt("held_trigger");
        this.blocksRemaining = nbt.getInt("blocks_remaining");
        this.enchantments = nbt.getList("enchantments", Tag.TAG_COMPOUND);
    }

    public Matrix4f getTransform()
    {
        return transform.transform;
    }

    public boolean triggerHeld()
    {
        return heldTrigger != -1;
    }

    @Override
    protected boolean consume(Container inventory, Player player, InteractionHand hand)
    {
        cooldown = 10; // Minimal cooldown for reloading
        return consume(1, inventory, player, hand);
    }

    public interface SetBreakProgress
    {
        void setBreakProgress(boolean stop, BlockPos pos, float progress);
    }
}
