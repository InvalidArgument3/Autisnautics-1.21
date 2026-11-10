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

package com.neep.neepmeat.machine.phage_ray;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.graphics.GraphicsEffects;
import com.neep.meatlib.network.GlobalChannelManager;
import com.neep.meatlib.util.ClientComponents;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.entity.SimpleEntity;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.init.NMGraphicsEffects;
import com.neep.neepmeat.machine.live_machine.process.PhageRayProcess;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PhageRayEntity extends SimpleEntity implements PhageRayProcess.PhageRay
{
    public static final GlobalChannelManager<BooleanConsumer> TRIGGER_C2S = GlobalChannelManager.create(
            ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "phage_ray/trigger"), ChannelFormat.BOOLEAN);

    protected static final EntityDataAccessor<Boolean> RUNNING = SynchedEntityData.defineId(PhageRayEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> FIRING = SynchedEntityData.defineId(PhageRayEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> BOTTOM = SynchedEntityData.defineId(PhageRayEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Integer> RANGE = SynchedEntityData.defineId(PhageRayEntity.class, EntityDataSerializers.INT);

    @Nullable private PhageRayBlockEntity parent;

    boolean trigger = false;
    public boolean externalTrigger = false;
    int triggerTicks = 0;

    private final ClientComponents.Holder<PhageRayEntity> holder = new ClientComponents.EntityHolder<>(this);

    public PhageRayEntity(EntityType<?> type, Level world)
    {
        super(type, world);
        blocksBuilding = true;
    }

    private void syncBeamEffect(ServerPlayer player, Level world, Vec3 start, Vec3 end, Vec3 velocity, float scale, int maxTime)
    {
        if (level().isClientSide)
            throw new IllegalStateException("packet create called on the client!");

        FriendlyByteBuf byteBuf = PacketByteBufs.create();

        byteBuf.writeVarInt(getId());

        GraphicsEffects.CHANNEL.emitter(player)
                .apply(world.dimension(), NMGraphicsEffects.PHAGE_RAY, start, end, velocity, scale, maxTime, byteBuf);

    }

    @Override
    protected void defineSynchedData()
    {
        entityData.define(RUNNING, false);
        entityData.define(FIRING, false);
        entityData.define(BOTTOM, false);
        entityData.define(RANGE, 16);
    }

    @Override
    public boolean skipAttackInteraction(Entity attacker)
    {
        return super.skipAttackInteraction(attacker);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt)
    {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {

    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger()
    {
        return (LivingEntity) getFirstPassenger();
    }

    @Override
    public void tick()
    {
        super.tick();
        if (!level().isClientSide() && (tickCount > 5 && (parent == null || parent.isRemoved())))
        {
            remove(RemovalReason.DISCARDED);
        }

        if (isControlledByLocalInstance() && getFirstPassenger() != null)
        {
            this.yRotO = getYRot();
            this.xRotO = getXRot();
            this.setYRot(getFirstPassenger().getYRot());
            this.setXRot(limitPitch(getFirstPassenger().getXRot()));
            this.setRot(this.getYRot(), this.getXRot());
        }
        else if (isControlledByLocalInstance())
        {
            this.yRotO = getYRot();
            this.xRotO = getXRot();
        }

        interpolatePosition();


        if (level().isClientSide())
        {
            clientTick();
        }

        if (isRunning() && (trigger || externalTrigger))
        {
            ++triggerTicks;

            if (!hasExactlyOnePlayerPassenger())
                trigger = false;
        }
        else
        {
            triggerTicks = 0;
        }

    }

    // Called by PhageRayProcess
    public void tickProcess(boolean canHarvest, boolean silkTouch, Storage<ItemVariant> combinedItemOutput, TransactionContext transaction)
    {
        // Update running state
        boolean canRun = parent != null && parent.canRun();

        if (entityData.get(RUNNING) != canRun)
        {
            entityData.set(RUNNING, canRun);
        }

        if (isRunning() && (trigger || externalTrigger))
        {
            entityData.set(FIRING, true);
            if (!level().isClientSide())
            {
                spawnBeams();
                breakBlocks(canHarvest, silkTouch, combinedItemOutput, transaction);
            }
        }
        else
        {
            entityData.set(FIRING, false);
        }
    }

    private void spawnBeams()
    {
        int beamInterval = 10;
        if ((triggerTicks - 1) % beamInterval == 0)
        {
            for (ServerPlayer player : PlayerLookup.tracking(this))
            {
                syncBeamEffect(player, level(),
                        getBeamOrigin(), getBeamEnd(), Vec3.ZERO, 1f, beamInterval);
            }
        }
    }

    public boolean trigger()
    {
        return entityData.get(FIRING);
//        return trigger || externalTrigger;
    }

    private final HashMap<BlockPos, Float> targets = Maps.newHashMap();

    private Set<BlockPos> getTargets(Vec3 origin, Vec3 end)
    {
//        Vec3d v = new Vec3d(0, 0.5, 0);
//        Vec3d u = getRotationVector().crossProduct(v).normalize().multiply(0.5);
//
//        System.out.println("u: " + u + " v: " + v);


        var u = new Vec3(0, 0.5, 0).xRot(-getViewXRot(1)).yRot(-getViewYRot(1));
        var v = new Vec3(0.5, 0, 0).xRot(-getViewXRot(1)).yRot(-getViewYRot(1));

        var starts = List.of(
                origin.subtract(u).subtract(v),
                origin.subtract(u).add(v),
                origin.add(u).add(v),
                origin.add(u).subtract(v)
        );

        var ends = List.of(
                end.subtract(u).subtract(v),
                end.subtract(u).add(v),
                end.add(u).add(v),
                end.add(u).subtract(v)
        );

        Set<BlockPos> newTargets = Sets.newHashSet();
        for (int i = 0; i < 4; i++)
        {
            ClipContext context = new ClipContext(
                    starts.get(i),
                    ends.get(i),
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    this);

            BlockHitResult result = level().clip(context);

            if (result.getType() == HitResult.Type.BLOCK)
            {
                newTargets.add(result.getBlockPos());
                targets.putIfAbsent(result.getBlockPos(), 0.0f);
            }
        }

        return newTargets;
    }

    private void breakBlocks(boolean canHarvest, boolean silkTouch, Storage<ItemVariant> output, TransactionContext transaction)
    {
        Level world = level();
        var tool = Items.NETHERITE_PICKAXE.getDefaultInstance();
        if (silkTouch)
        {
            tool = tool.copy();
            tool.enchant(Enchantments.SILK_TOUCH, 1);
        }

        if (level().getGameTime() % 2 == 0)
        {
            Set<BlockPos> newTargets = getTargets(getBeamOrigin(), getBeamEnd());

            targets.entrySet().removeIf(e -> !newTargets.contains(e.getKey()));
        }

        Iterator<Map.Entry<BlockPos, Float>> it = targets.entrySet().iterator();
        while (it.hasNext())
        {
            var target = it.next();
            BlockPos pos = target.getKey();
            if (target.getValue() >= 1 && !level().getBlockState(target.getKey()).is(NMBlocks.PHAGE_RAY.getStructure()))
            {
                if (canHarvest)
                {
                    BlockState state = world.getBlockState(target.getKey());
                    LootParams.Builder builder = new LootParams.Builder((ServerLevel) world)
                            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                            .withParameter(LootContextParams.TOOL, tool)
                            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, null);

                    List<ItemStack> dropped = state.getDrops(builder);

                    for (var stack : dropped)
                    {
                        output.insert(ItemVariant.of(stack), stack.getCount(), transaction);
                    }
                }
                world.destroyBlock(target.getKey(), false);
                it.remove();
            }
            else
            {
                BlockState state = level().getBlockState(target.getKey());
                target.setValue(target.getValue() + calcBlockBreakingDelta(state, level(), target.getKey(), true, 20));
            }
        }
    }

    public static float calcBlockBreakingDelta(BlockState state, BlockGetter world, BlockPos pos, boolean allowOverride, float speed)
    {
        if (allowOverride && state.is(NMTags.PHAGE_RAY_OVERRIDE))
            return 0.1f;

        float f = state.getDestroySpeed(world, pos);
        if (f == -1.0f)
        {
            return 0.0f;
        }

        return speed / f / 30f;
    }

    void setPlayerTrigger(boolean trigger)
    {
        this.trigger = trigger;
        if (!trigger && parent != null)
        {
            // Allow resetting external trigger by player
            externalTrigger = false;
            parent.targetFiring = false;
        }
    }

    public void setRange(int range)
    {
        entityData.set(RANGE, range);
    }

    public Vec3 getBeamEnd()
    {
        return getBeamOrigin().add(getLookAngle().scale(entityData.get(RANGE)));
    }

    @Override
    public Vec3 getBeamOrigin()
    {
        return position().add(0, 1.5, 0);
    }

    @Override
    public Vec3 getClientBeamEnd(Vec3 start, float tickDelta)
    {
        var end = start.add(getViewVector(tickDelta).scale(entityData.get(RANGE)));

        ClipContext context = new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this);

        BlockHitResult result = level().clip(context);

        return result.getLocation();
    }

    @Override
    public int getAge()
    {
        return tickCount;
    }

    private float limitPitch(float pitch)
    {
        return entityData.get(BOTTOM) ?
                Mth.clamp(pitch, -30, 90)
                : Mth.clamp(pitch, -90, 30);
    }

    public boolean isPickable()
    {
        return !this.isRemoved();
    }

    @Override
    protected void addPassenger(Entity passenger)
    {
        super.addPassenger(passenger);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger)
    {
        return position().add(0, getBbHeight(), 0);
//        Vec3d vec3d = AbstractVehicleEntity.getPassengerDismountOffset(this.getWidth() * MathHelper.SQUARE_ROOT_OF_TWO, passenger.getWidth(), passenger.getYaw());
//        double posX = this.getX() + vec3d.x;
//        double posZ = this.getZ() + vec3d.z;
//        BlockPos blockPos = new BlockPos(posX, this.getBoundingBox().maxY, posZ).down();
//        if (!this.getWorld().isWater(blockPos))
//        {
//            ArrayList<Vec3d> list = Lists.newArrayList();
//            double f = this.getWorld().getDismountHeight(blockPos);
//            if (Dismounting.canDismountInBlock(f))
//            {
//                list.add(new Vec3d(posX, blockPos.getY() + f, posZ));
//            }
//            double g = this.getWorld().getDismountHeight(blockPos);
//            if (Dismounting.canDismountInBlock(g))
//            {
//                list.add(new Vec3d(posX, blockPos.getY() + g, posZ));
//            }
//            for (EntityPose entityPose : passenger.getPoses())
//            {
//                for (Vec3d vec3d2 : list)
//                {
//                    if (!Dismounting.canPlaceEntityAt(this.world, vec3d2, passenger, entityPose))
//                        continue;
//                    passenger.setPose(entityPose);
//                    return vec3d2;
//                }
//            }
//        }
//        return super.updatePassengerForDismount(passenger);
    }

    @Override
    public boolean canChangeDimensions()
    {
        return false;
    }

    @Override
    public void move(MoverType movementType, Vec3 movement)
    {
        super.move(movementType, movement);
    }

    @Override
    public boolean canCollideWith(Entity other)
    {
        return super.canCollideWith(other);
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }

    public boolean isRunning()
    {
        return entityData.get(RUNNING);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand)
    {
        if (!level().isClientSide())
        {
            if (isVehicle())
                return InteractionResult.PASS;

            return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction positionUpdater)
    {
        if (this.hasPassenger(passenger))
        {
            Vec3 v = new Vec3(2, 0.0, 0.0).yRot((float) (-Math.toRadians(this.getYRot())));
            v = v.add(getX(), getY() + 1, getZ());
            positionUpdater.accept(passenger, v.x, v.y, v.z);
        }
    }
    @Override
    public void onPassengerTurned(Entity passenger)
    {
        super.onPassengerTurned(passenger);
//        this.setYaw(passenger.getYaw());
//        this.setPitch(limitPitch(passenger.getPitch()));
    }
    //    @Override
//    public double getMountedHeightOffset()
//    {

//        return geth;

//    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket()
    {
        return new ClientboundAddEntityPacket(this);
    }

    public void setParent(PhageRayBlockEntity phageRayBlockEntity)
    {
        this.parent = phageRayBlockEntity;
        entityData.set(BOTTOM, parent.isBottom());
    }

    public boolean isBottom()
    {
        return entityData.get(BOTTOM);
    }

    static
    {
        TRIGGER_C2S.receiverHandler(
                EnvType.SERVER, (player, buf, responseSender) -> trigger ->
                    {
                        if (player.getVehicle() instanceof PhageRayEntity phageRay)
                        {
                            phageRay.setPlayerTrigger(trigger);
                        }
                    });
    }

    public void clientTick()
    {
        holder.get().clientTick();
    }

    @Nullable
    @Override
    public ItemStack getPickResult()
    {
        return NMBlocks.PHAGE_RAY.asItem().getDefaultInstance();
    }

}
