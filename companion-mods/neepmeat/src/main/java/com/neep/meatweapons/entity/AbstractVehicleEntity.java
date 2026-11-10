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

package com.neep.meatweapons.entity;

import com.google.common.collect.Lists;
import com.neep.meatweapons.client.MWKeys;
import net.minecraft.BlockUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractVehicleEntity
extends Entity {
    protected float velocityDecay;
    protected float yawVelocity;
    protected float roll;
    protected float prevRoll;
    private int delta;
    private double x;
    private double y;
    private double z;
    protected double vehicleYaw;
    protected double vehiclePitch;

    protected boolean pressingLeft;
    protected boolean pressingRight;
    protected boolean pressingForward;
    protected boolean pressingBack;
    protected boolean pressingUp;
    protected boolean pressingDown;
    protected boolean prevForward;
    protected boolean prevBack;
    protected boolean prevLeft;
    protected boolean prevRight;
    protected boolean prevUp;
    protected boolean prevDown;

    protected double fallVelocity;
    protected boolean powered = true;

    protected int health;

    private static final EntityDataAccessor<Float> ROLL = SynchedEntityData.defineId(AbstractVehicleEntity.class, EntityDataSerializers.FLOAT);

    public AbstractVehicleEntity(EntityType<? extends Entity> entityType, Level world)
    {
        super(entityType, world);
        this.blocksBuilding = true;
        this.health = 15;
    }

    public abstract ItemStack asStack();

    public abstract SoundEvent getDamageSound();

    @Override
    protected float getEyeHeight(Pose pose, EntityDimensions dimensions)
    {
        return dimensions.height;
    }

    @Override
    protected MovementEmission getMovementEmission()
    {
        return MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        this.entityData.define(ROLL, 0f);
    }

    @Override
    public boolean canCollideWith(Entity other)
    {
        return AbstractVehicleEntity.canCollide(this, other);
    }

    public static boolean canCollide(Entity entity, Entity other)
    {
        return (other.canBeCollidedWith() || other.isPushable()) && !entity.isPassengerOfSameVehicle(other);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected Vec3 getRelativePortalPosition(Direction.Axis portalAxis, BlockUtil.FoundRectangle portalRect)
    {
        return LivingEntity.resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(portalAxis, portalRect));
    }

    @Override
    public double getPassengersRidingOffset()
    {
        return 0.2;
    }

    @Override
    public boolean hurt(DamageSource source, float amount)
    {
        if (this.level().isClientSide || this.isRemoved())
        {
            return true;
        }

        this.health -= (int) amount;

        this.markHurt();
        this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
        level().playSound(null, this, getDamageSound(), SoundSource.NEUTRAL, 1, 1);

        if (health <= 0)
        {
            dropDead();
            this.discard();
        }
        return true;
    }

    @Override
    public void push(Entity entity)
    {
        if (entity instanceof AbstractVehicleEntity)
        {
            if (entity.getBoundingBox().minY < this.getBoundingBox().maxY)
            {
                super.push(entity);
            }
        } else if (entity.getBoundingBox().minY <= this.getBoundingBox().minY)
        {
            super.push(entity);
        }
    }

//    @Override
//    public void animateDamage()
//    {
//        getEntityWorld().addParticle(ParticleTypes.SMOKE, this.x, this.y, this.z, 0, 0, 0);
//    }

    @Override
    public boolean isPickable()
    {
        return !this.isRemoved();
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.vehicleYaw = yaw;
        this.vehiclePitch = pitch;
        this.delta = 10;
    }

    @Override
    public Direction getMotionDirection() {
        return this.getDirection().getClockWise();
    }

    @Override
    public void tick()
    {
        super.tick();
        this.interpolatePosition();
        if (this.isControlledByLocalInstance())
        {
            this.updateVelocity();
            if (this.level().isClientSide)
            {
                updateKeys();
                this.updateMotion();
            }
            this.move(MoverType.SELF, this.getDeltaMovement());
        }
        else
        {
            this.setDeltaMovement(Vec3.ZERO);
        }

        this.checkInsideBlocks();

        List<Entity> otherEntities = this.level().getEntities(this, this.getBoundingBox().inflate(0.2f, -0.01f, 0.2f), EntitySelector.pushableBy(this));
        if (!otherEntities.isEmpty()) {
            boolean soundEvent = !this.level().isClientSide && !(getControllingPassenger() instanceof Player);
            for (Entity entity : otherEntities)
            {
                if (entity.hasPassenger(this))
                    continue;
                if (soundEvent && this.getPassengers().size() < 2 && !entity.isPassenger() && entity.getBbWidth() < this.getBbWidth() && entity instanceof LivingEntity && !(entity instanceof WaterAnimal) && !(entity instanceof Player))
                {
                    entity.startRiding(this);
                    continue;
                }
                this.push(entity);
            }
        }
        calculateRoll();
    }

    private void interpolatePosition()
    {
        if (this.isControlledByLocalInstance())
        {
            this.delta = 0;
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
        }
        if (this.delta <= 0)
        {
            return;
        }
        double dx = this.getX() + (this.x - this.getX()) / (double) this.delta;
        double dy = this.getY() + (this.y - this.getY()) / (double) this.delta;
        double dz = this.getZ() + (this.z - this.getZ()) / (double) this.delta;
        double dyaw = Mth.wrapDegrees(this.vehicleYaw - (double) this.getYRot());
        this.setYRot(this.getYRot() + (float) dyaw / (float)this.delta);
        this.setXRot(this.getXRot() + (float) (this.vehiclePitch - (double) this.getXRot()) / (float) this.delta);
        --this.delta;
        this.setPos(dx, dy, dz);
        this.setRot(this.getYRot(), this.getXRot());
    }

    private void updateVelocity()
    {
        this.velocityDecay = this.onGround() ? 0.2f : 0.9f;
        float verticalVelocityDecay = 0.8f;

        Vec3 vec3d = this.getDeltaMovement();
        this.setDeltaMovement(vec3d.x * (double)this.velocityDecay, vec3d.y * verticalVelocityDecay, vec3d.z * (double)this.velocityDecay);
        this.yawVelocity *= this.velocityDecay;
    }

    // This is a terrible idea. Put this in a client-only class.
    protected void updateKeys()
    {
        // TODO: Why does this not crash on a server?
        Options options = Minecraft.getInstance().options;

        this.prevForward = pressingForward;
        this.prevBack = pressingBack;
        this.prevLeft = pressingLeft;
        this.prevRight = pressingRight;
        this.prevUp = pressingUp;
        this.prevDown = pressingDown;

        this.pressingForward = options.keyUp.isDown();
        this.pressingBack = options.keyDown.isDown();
        this.pressingLeft = options.keyLeft.isDown();
        this.pressingRight = options.keyRight.isDown();
        this.pressingUp = options.keyJump.isDown();
        this.pressingDown = MWKeys.AIRTRUCK_DOWN.isDown();
    }

    protected void updateMotion()
    {
        if (!this.isVehicle())
        {
            return;
        }

        float forwardsVelocity = 0.0f;
        float upVelocity = 0.0f;
        if (this.pressingLeft)
        {
            this.yawVelocity -= 1.0f;
        }
        if (this.pressingRight)
        {
            this.yawVelocity += 1.0f;
        }
        if (this.pressingRight != this.pressingLeft && !this.pressingForward && !this.pressingBack)
        {
            forwardsVelocity += 0.005f;
        }
        this.setYRot(this.getYRot() + this.yawVelocity);
        if (this.pressingForward)
        {
            forwardsVelocity += 0.04f;
        }
        if (this.pressingBack)
        {
            forwardsVelocity -= 0.005f;
        }
        if (this.pressingUp)
        {
            upVelocity += 0.08f;
        }
        if (this.pressingDown)
        {
            upVelocity -= 0.08f;
        }
        this.setDeltaMovement(this.getDeltaMovement().add(Mth.sin(-this.getYRot() * ((float)Math.PI / 180)) * forwardsVelocity,
                upVelocity,
                Mth.cos(this.getYRot() * ((float)Math.PI / 180)) * forwardsVelocity));
    }

    @Override
    public void positionRider(Entity passenger, Entity.MoveFunction updater)
    {
        if (!this.hasPassenger(passenger))
        {
            return;
        }

        float driverOffset = 1.0f;
        float trailerOffset = -1f;
        float xOffset = driverOffset;
        float yOffset = (float) (this.getPassengersRidingOffset() + passenger.getMyRidingOffset());

        if (this.getPassengers().size() > 1)
        {
            int i = this.getPassengers().indexOf(passenger);
            xOffset = i == 0 ? driverOffset : trailerOffset;
            if (passenger instanceof Animal)
            {
                xOffset = xOffset + 0.2f;
            }
        }

        Vec3 i = new Vec3(xOffset, 0.0, 0.0).yRot((float) (-this.getYRot() * Math.PI / 180 - Math.PI / 2));

        passenger.setPos(this.getX() + i.x, this.getY() + yOffset, this.getZ() + i.z);
        passenger.setYRot(passenger.getYRot() + this.yawVelocity);
        passenger.setYHeadRot(passenger.getYHeadRot() + this.yawVelocity);

        this.copyEntityData(passenger);

        if (passenger instanceof Animal && this.getPassengers().size() > 1)
        {
            int animalYaw = passenger.getId() % 2 == 0 ? 90 : 270;
            passenger.setYBodyRot(((Animal)passenger).yBodyRot + animalYaw);
            passenger.setYHeadRot(passenger.getYHeadRot() + animalYaw);
        }
    }

    protected void copyEntityData(Entity entity)
    {
        entity.setYBodyRot(this.getYRot());
        float f = Mth.wrapDegrees(entity.getYRot() - this.getYRot());
        float g = Mth.clamp(f, -105.0f, 105.0f);
        entity.yRotO += g - f;
        entity.setYRot(entity.getYRot() + g - f);
        entity.setYHeadRot(entity.getYRot());
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger)
    {
        double e;
        Vec3 vec3d = AbstractVehicleEntity.getCollisionHorizontalEscapeVector(this.getBbWidth() * Mth.SQRT_OF_TWO, passenger.getBbWidth(), passenger.getYRot());
        double d = this.getX() + vec3d.x;
        BlockPos blockPos = new BlockPos((int) d, (int) this.getBoundingBox().maxY, (int) (e = this.getZ() + vec3d.z)).below();
        if (!this.level().isWaterAt(blockPos))
        {
            double g;
            ArrayList<Vec3> list = Lists.newArrayList();
            double f = this.level().getBlockFloorHeight(blockPos);
            if (DismountHelper.isBlockFloorValid(f)) {
                list.add(new Vec3(d, (double)blockPos.getY() + f, e));
            }
            if (DismountHelper.isBlockFloorValid(g = this.level().getBlockFloorHeight(blockPos)))
            {
                list.add(new Vec3(d, (double)blockPos.getY() + g, e));
            }
            for (Pose entityPose : passenger.getDismountPoses())
            {
                for (Vec3 vec3d2 : list) {
                    if (!DismountHelper.canDismountTo(level(), vec3d2, passenger, entityPose)) continue;
                    passenger.setPose(entityPose);
                    return vec3d2;
                }
            }
        }
        return super.getDismountLocationForPassenger(passenger);
    }

    @Override
    public void onPassengerTurned(Entity passenger)
    {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt)
    {
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand)
    {
        if (!this.level().isClientSide)
        {
            if (player.isShiftKeyDown())
            {
                dropDead();
                discard();
                return InteractionResult.SUCCESS;
            }

            return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void checkFallDamage(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition)
    {
        this.fallVelocity = this.getDeltaMovement().y;
        if (this.isPassenger())
        {
            return;
        }
        if (onGround)
        {
            if (this.fallDistance > 3.0f && !powered)
            {
//                this.handleFallDamage(this.fallDistance, 1.0f, getWorld().getDamageSources().fall());
                this.causeFallDamage(this.fallDistance, 1.0f, getCommandSenderWorld().damageSources().fall());
                if (!this.level().isClientSide && !this.isRemoved())
                {
                    this.kill();
                    this.dropDead();
                }
            }
            this.fallDistance = 0.0f;
        }
        else if (!this.level().getFluidState(this.blockPosition().below()).is(FluidTags.WATER) && heightDifference < 0.0)
        {
            this.fallDistance = (float)((double)this.fallDistance - heightDifference);
        }
    }

    public void calculateRoll()
    {
        this.prevRoll = getRoll();

        // This replaces an in-depth calculation of approximate turning radius, centripetal acceleration and some rigid-body magic
        this.setRoll((float) (getDeltaMovement().horizontalDistance() * yawVelocity));
        this.setXRot((float) ((float) - getDeltaMovement().y() * getDeltaMovement().horizontalDistance() * 10));
    }

    @Override
    protected boolean canAddPassenger(Entity passenger)
    {
        return this.getPassengers().size() < 2;
    }

    @Override
    public LivingEntity getControllingPassenger()
    {
        return this.getFirstPassenger() instanceof LivingEntity living ? living : null;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket()
    {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public ItemStack getPickResult()
    {
        return this.asStack();
    }

    public void dropDead()
    {
        this.spawnAtLocation(asStack());
    }

    public void setRoll(float roll)
    {
        this.roll = roll;
        this.getEntityData().set(ROLL, roll);
    }

    public float getRoll()
    {
        return this.roll;
    }

    public float getRoll(float tickDelta)
    {
        return Mth.lerp(tickDelta, this.prevRoll, this.getRoll());
    }
}

