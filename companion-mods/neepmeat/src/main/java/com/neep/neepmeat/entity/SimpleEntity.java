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

package com.neep.neepmeat.entity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class SimpleEntity extends Entity
{
    private static final EntityDataAccessor<Float> SIDEWAYS_SPEED = SynchedEntityData.defineId(SimpleEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> FORWARD_SPEED = SynchedEntityData.defineId(SimpleEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> UPWARD_SPEED = SynchedEntityData.defineId(SimpleEntity.class, EntityDataSerializers.FLOAT);

    protected int trackingIncrements;
    protected double serverX;
    protected double serverY;
    protected double serverZ;
    protected double serverPitch;
    protected double serverYaw;

    private float sidewaysSpeed;
    private float forwardSpeed;
    private float upwardSpeed;

    public SimpleEntity(EntityType<?> type, Level world)
    {
        super(type, world);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        this.entityData.define(SIDEWAYS_SPEED, 0f);
        this.entityData.define(FORWARD_SPEED, 0f);
        this.entityData.define(UPWARD_SPEED, 0f);
    }

    protected float getBaseMovementSpeedMultiplier()
    {
        return 0.8f;
    }

    public boolean shouldSwim()
    {
        return false;
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate)
    {
        this.serverX = x;
        this.serverY = y;
        this.serverZ = z;
        this.serverYaw = yaw;
        this.serverPitch = pitch;
        this.trackingIncrements = interpolationSteps;
    }

    protected void interpolatePosition()
    {
        if (this.trackingIncrements > 0)
        {
            double d = this.getX() + (this.serverX - this.getX()) / (double) this.trackingIncrements;
            double e = this.getY() + (this.serverY - this.getY()) / (double) this.trackingIncrements;
            double f = this.getZ() + (this.serverZ - this.getZ()) / (double) this.trackingIncrements;
            double g = Mth.wrapDegrees(this.serverYaw - (double)this.getYRot());
            this.setYRot(this.getYRot() + (float) g / (float) this.trackingIncrements);
            this.setXRot(this.getXRot() + (float)(this.serverPitch - (double) this.getXRot()) / (float) this.trackingIncrements);

            --this.trackingIncrements;

            this.setPos(d, e, f);
            this.setRot(this.getYRot(), this.getXRot());
        }
    }

    public void tickMovement()
    {
        if (this.isControlledByLocalInstance())
        {
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
        }

        interpolatePosition();

        Vec3 vec3d = this.getDeltaMovement();
        double h = vec3d.x;
        double i = vec3d.y;
        double j = vec3d.z;

        // Cut off at low speeds
        if (Math.abs(vec3d.x) < 0.003) h = 0.0;
        if (Math.abs(vec3d.y) < 0.003) i = 0.0;
        if (Math.abs(vec3d.z) < 0.003) j = 0.0;

        this.setDeltaMovement(h, i, j);

        this.level().getProfiler().push("travel");
        this.sidewaysSpeed *= 0.98f;
        this.forwardSpeed *= 0.98f;
        this.travel(new Vec3(this.sidewaysSpeed, this.upwardSpeed, this.forwardSpeed));
        this.level().getProfiler().pop();

        this.level().getProfiler().push("push");
        tickCramming();
        this.level().getProfiler().pop();
    }

    public Vec3 applyVerticalSpeed(double d, boolean bl, Vec3 vec3d)
    {
        if (!this.isNoGravity() && !this.isSprinting())
        {
            double e = bl && Math.abs(vec3d.y - 0.005) >= 0.003 && Math.abs(vec3d.y - d / 16.0) < 0.003 ? -0.003 : vec3d.y - d / 16.0;
            return new Vec3(vec3d.x, e, vec3d.z);
        }
        return vec3d;
    }

    protected void swimInWater(Vec3 movementInput)
    {
        double y = this.getY();
        float forward = this.isSprinting() ? 0.9f : this.getBaseMovementSpeedMultiplier();
        float g = 0.02f;
        this.moveRelative(g, movementInput);
        this.move(MoverType.SELF, this.getDeltaMovement());
        Vec3 vec3d = this.getDeltaMovement();
        this.setDeltaMovement(vec3d.multiply(forward, 0.8f, forward));
        Vec3 vec3d2 = this.applyVerticalSpeed(0.08f, false, this.getDeltaMovement());
        this.setDeltaMovement(vec3d2);
        if (this.horizontalCollision && this.isFree(vec3d2.x, vec3d2.y + (double)0.6f - this.getY() + y, vec3d2.z))
        {
            this.setDeltaMovement(vec3d2.x, 0.3f, vec3d2.z);
        }
    }

    protected void swimInLava(Vec3 movementInput, double d)
    {
        Vec3 vec3d3;
        double e = this.getY();
        this.moveRelative(0.02f, movementInput);
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (this.getFluidHeight(FluidTags.LAVA) <= this.getFluidJumpThreshold())
        {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.8f, 0.5));
            vec3d3 = this.applyVerticalSpeed(d, false, this.getDeltaMovement());
            this.setDeltaMovement(vec3d3);
        }
        else
        {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
        }
        if (!this.isNoGravity())
        {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -d / 4.0, 0.0));
        }
        vec3d3 = this.getDeltaMovement();
        if (this.horizontalCollision && this.isFree(vec3d3.x, vec3d3.y + 0.6f - this.getY() + e, vec3d3.z))
        {
            this.setDeltaMovement(vec3d3.x, 0.3f, vec3d3.z);
        }
    }

    protected void moveOnLand(Vec3 movementInput, double d)
    {
        BlockPos blockPos = this.getBlockPosBelowThatAffectsMyMovement();
        float slipperiness = this.level().getBlockState(blockPos).getBlock().getFriction();
        float f = this.onGround() ? slipperiness * 0.91f : 0.91f;
        Vec3 vec3d6 = this.applyMovementInput(movementInput, slipperiness);
        double q = vec3d6.y;
        if (!this.level().isClientSide || this.level().hasChunkAt(blockPos))
        {
            if (!this.isNoGravity())
            {
                q -= d;
            }
        }
        else
        {
            q = this.getY() > (double)this.level().getMinBuildHeight() ? -0.1 : 0.0;
        }
        this.setDeltaMovement(vec3d6.x * (double)f, q * (double)0.98f, vec3d6.z * (double)f);
    }

    public void travel(Vec3 movementInput)
    {
        if (this.isControlledByLocalInstance())
        {
            double d = 0.08;
            if (this.isInWater() && this.shouldSwim())
            {
                swimInWater(movementInput);
            }
            else if (this.isInLava() && shouldSwim())
            {
                swimInLava(movementInput, d);
            }
            else
            {
                moveOnLand(movementInput, d);
            }
        }
    }

    public Vec3 applyMovementInput(Vec3 movementInput, float slipperiness)
    {
        this.moveRelative(this.getMovementSpeed(slipperiness), movementInput);
//        this.setVelocity(this.applyClimbingSpeed(this.getVelocity()));
        this.move(MoverType.SELF, this.getDeltaMovement());
        //        if ((this.horizontalCollision) && (g() || this.getBlockStateAtPos().isOf(Blocks.POWDER_SNOW) && PowderSnowBlock.canWalkOnPowderSnow(this))) {
//            vec3d = new Vec3d(vec3d.x, 0.2, vec3d.z);
//        }
        return this.getDeltaMovement();
    }

    public float getMovementSpeed()
    {
        return 0.1f;
    }

    private float getMovementSpeed(float slipperiness)
    {
        return this.getMovementSpeed() * (0.2f / (slipperiness * slipperiness * slipperiness));
    }

    protected void tickCramming()
    {
        List<Entity> list = this.level().getEntities(this, this.getBoundingBox(), EntitySelector.pushableBy(this));
        if (!list.isEmpty())
        {
            int j;
            int i = this.level().getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
            if (i > 0 && list.size() > i - 1 && this.random.nextInt(4) == 0)
            {
                j = 0;
                for (Entity entity : list)
                {
                    if (entity.isPassenger()) continue;
                    ++j;
                }
                if (j > i - 1)
                {
                    this.hurt(level().damageSources().cramming(), 6.0f);
                }
            }
            for (j = 0; j < list.size(); ++j)
            {
                Entity entity = list.get(j);
                this.push(entity);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt)
    {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {

    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket()
    {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public boolean isPickable()
    {
        return true;
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return false;
    }

    @Override
    public boolean canCollideWith(Entity other)
    {
        return other.canBeCollidedWith();
    }

    @Override
    public boolean isPushable()
    {
        return true;
    }
}
