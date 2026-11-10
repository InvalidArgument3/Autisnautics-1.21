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

package com.neep.meatweapons.item;

import com.neep.neepmeat.item.ClientBlockAttackListener;
import com.neep.meatlib.item.CustomEnchantable;
import com.neep.meatlib.item.MeatlibItem;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.item.PoweredItem;
import com.neep.meatweapons.MWItems;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.client.sound.DrillSoundInstance;
import com.neep.meatweapons.component.AssaultDrillComponent;
import com.neep.meatweapons.entity.BulletDamageSource;
import com.neep.meatweapons.init.MWComponents;
import com.neep.neepmeat.api.item.OverrideSwingItem;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.item.util.LivingItemStorage;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class AssaultDrillItem extends Item implements MeatlibItem, PoweredItem, CustomEnchantable, OverrideSwingItem, ClientBlockAttackListener
{
    public static final ResourceLocation CHANNEL_ID = ResourceLocation.parse("assault_drill");

    private final TagKey<Block> effectiveBlocks;
    private final float miningSpeed;
    protected float attackDamage;

    public AssaultDrillItem(int maxDamage, FabricItemSettings settings)
    {
        super(settings.stacksTo(1).durability(maxDamage));

        this.attackDamage = 1.5f;
        this.effectiveBlocks = BlockTags.MINEABLE_WITH_PICKAXE;
        this.miningSpeed = Tiers.DIAMOND.getSpeed();
    }

    public static boolean using(ItemStack stack)
    {
        AssaultDrillComponent component = MWComponents.ASSAULT_DRILL.getNullable(stack);
        if (component != null)
        {
            return component.using() || component.attacking();
        }

        return false;
    }

    public static Storage<FluidVariant> getStorage(ItemStack stack, ContainerItemContext containerItemContext)
    {
        return new LivingItemStorage(stack, containerItemContext);
    }

    public void appendHoverText(ItemStack itemStack, Level world, List<Component> tooltip, TooltipFlag tooltipContext)
    {
        TooltipSupplier.wrapLines(tooltip, Component.translatable("item." + MeatWeapons.NAMESPACE + "." + "assault_drill" + ".lore"));
        tooltip.add(Component.translatable("item." + MeatWeapons.NAMESPACE + "." + "assault_drill" + ".damage_per_tick", getDamage(itemStack, null) / 2f).withStyle(ChatFormatting.BLUE));
    }

    public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack)
    {
        return false;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.NONE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand)
    {
        ItemStack itemStack = user.getItemInHand(hand);
        user.startUsingItem(hand);

        MWComponents.ASSAULT_DRILL.maybeGet(itemStack).ifPresent(c -> c.setUsing(true));

        return InteractionResultHolder.fail(itemStack);
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks)
    {
        boolean canUse = true;
        if (stack.getDamageValue() >= stack.getMaxDamage())
        {
            MWComponents.ASSAULT_DRILL.maybeGet(stack).ifPresent(c -> c.setUsing(false));
            canUse = false;
        }

        double radius = 1.3;
        double distance = 1.2;
        Vec3 tip = user.getEyePosition().add(user.getViewVector(1).normalize().scale(distance));
        AABB box = AABB.ofSize(tip, radius, radius, radius);
        if (world instanceof ServerLevel serverWorld && canUse)
        {
            // Damage all entities within a small box at the end of the drill model
            List<Entity> entities = world.getEntities(user, box, e -> true);
            if (!entities.isEmpty())
            {
                entities.forEach(entity ->
                {
                    if (entity instanceof LivingEntity && entity.isAlive() && user instanceof Player player)
                    {
                        // Set entity damage cooldown to 4 ticks (20 - 14)
                        entity.hurt(BulletDamageSource.create(world, player, 0.04f, 14, false, 0), getDamage(stack, entity));

                        // Spawn blood particles
                        serverWorld.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.NETHER_WART_BLOCK.defaultBlockState()), tip.x, tip.y, tip.z, 3, 0.05, 0.05, 0.05, 0.2);
                    }
                });
            }

            // Reduce durability
            if (!(user instanceof Player player && player.isCreative())
                    && stack.getDamageValue() < stack.getMaxDamage())
            {
                stack.setDamageValue(stack.getDamageValue() + 1);
            }
        }


        super.onUseTick(world, user, stack, remainingUseTicks);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner)
    {
        if (!world.isClientSide && state.getDestroySpeed(world, pos) != 0.0F && stack.getDamageValue() < stack.getMaxDamage())
        {
            if (stack.getDamageValue() == stack.getMaxDamage() - 1)
                stack.setDamageValue(stack.getMaxDamage());
            else
                stack.hurtAndBreak(1, miner, e -> {});
        }
        return true;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack)
    {
        return super.getTooltipImage(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks)
    {
        MWComponents.ASSAULT_DRILL.maybeGet(stack).ifPresent(c -> c.setUsing(false));
    }

    protected float getDamage(ItemStack stack, @Nullable Entity target)
    {
        float damage = target instanceof LivingEntity livingTarget ?
                EnchantmentHelper.getDamageBonus(stack, livingTarget.getMobType()) : EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);

        return attackDamage + damage;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state)
    {
        if (stack.getDamageValue() >= stack.getMaxDamage())
        {
            return 1;
        }

        return state.is(this.effectiveBlocks) ? this.miningSpeed * 4 : 1;
    }

    public boolean isCorrectToolForDrops(BlockState state)
    {
        return state.is(this.effectiveBlocks);
    }

    @Override
    public int getBarWidth(ItemStack stack)
    {
        return super.getBarWidth(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack)
    {
        return true;
    }

    public boolean canBeDepleted()
    {
        return false;
    }

    @Override
    public int getBarColor(ItemStack stack)
    {
        return PLCCols.MEAT_ITEM_BAR_COL;
    }

    @Override
    public boolean isEnchantable(ItemStack stack)
    {
        return true;
    }

    @Override
    public int getEnchantmentValue()
    {
        return 1;
    }

    @Override
    public void onAttackBlock(ItemStack stack, Player player)
    {
        if (stack.getDamageValue() < stack.getMaxDamage())
            sendAttack(true);
    }

    @Override
    public void onFinishAttackBlock(ItemStack stack, Player player)
    {
        sendAttack(false);
    }

    @Override
    public boolean onSwing(ItemStack stack, Player player)
    {
        player.attackAnim = 0;
        return false;
    }

    static
    {
        ServerPlayNetworking.registerGlobalReceiver(CHANNEL_ID, AssaultDrillItem::onAttackPacket);
    }

    private void sendAttack(boolean attacking)
    {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(attacking);
        ClientPlayNetworking.send(CHANNEL_ID, buf);
    }

    private static void onAttackPacket(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender)
    {
        boolean attacking = buf.readBoolean();

        server.execute(() ->
        {
            ItemStack mainStack = player.getMainHandItem();
            if (mainStack.getItem() instanceof AssaultDrillItem)
            {
                MWComponents.ASSAULT_DRILL.maybeGet(mainStack).ifPresent(c -> c.setAttacking(attacking));
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public static class Client
    {
        private static final DrillSoundInstance soundInstance = new DrillSoundInstance();

        public static void init()
        {
            ClientTickEvents.START_CLIENT_TICK.register(Client::tick);
        }

        public static void tick(Minecraft client)
        {
            SoundManager manager = client.getSoundManager();
            Player player = client.player;
            if (player != null)
            {
                if (player.getItemInHand(InteractionHand.MAIN_HAND).is(MWItems.ASSAULT_DRILL))
                {
                    if (!manager.isActive(soundInstance))
                    {
                        soundInstance.setPlayer(player);
                        manager.play(soundInstance);
                    }
                }
                else if (manager.isActive(soundInstance))
                {
                    manager.stop(soundInstance);
                }
            }
        }
    }
}