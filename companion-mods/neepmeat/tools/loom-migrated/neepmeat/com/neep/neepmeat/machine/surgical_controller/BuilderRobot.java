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

package com.neep.neepmeat.machine.surgical_controller;

import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.plc.block.BuilderBlock;
import com.neep.neepmeat.plc.block.entity.BuilderBlockEntity;
import com.neep.neepmeat.plc.robot.PLCRobot;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.neep.neepmeat.machine.surgical_controller.SurgicalRobot.MovementState.*;

public class BuilderRobot implements PLCRobot, NbtSerialisable
{
    private final BuilderBlockEntity parent;
    private SurgicalRobot.MovementState movementState = SurgicalRobot.MovementState.STATE_IDLE;

    private boolean building = false;

    private boolean initial = true;

    private double x;
    private double y;
    private double z;
    public double clientX;
    public double clientY;
    public double clientZ;
    public float clientYaw;

    public double prevX;
    public double prevY;
    public double prevZ;

    @Nullable private Vec3 targetPos;
    @Nullable private Vec3 actualTargetPos;
    @Nullable private BlockPos target;

    private final BlockPos basePos;
    private final Vec3 dockingPos;
    private final Vec3 attachPos;

    private float yaw;

    // I can't be bothered to do this properly
    public ResourceAmount<ItemVariant> stored;
    public boolean posDirty;

    public BuilderRobot(BuilderBlockEntity parent)
    {
        this.parent = parent;
        this.yaw = parent.getBlockState().getValue(BuilderBlock.FACING).toYRot();
        this.basePos = parent.getBlockPos();
        this.dockingPos = Vec3.upFromBottomCenterOf(basePos, 0.7);
        this.attachPos = Vec3.upFromBottomCenterOf(basePos, 1.4);
        this.x = dockingPos.x;
        this.y = dockingPos.y;
        this.z = dockingPos.z;

        this.clientX = x;
        this.clientY = y;
        this.clientZ = z;
        this.clientYaw = yaw;
    }

    public boolean shouldUpdatePosition(Level world)
    {
        if (!world.isClientSide())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public void tick()
    {
        if (shouldUpdatePosition(parent.getLevel()))
        {
            move();
        }
    }

    protected void move()
    {
        if (movementState == SurgicalRobot.MovementState.STATE_IDLE)
        {
            if (!Objects.equals(target, basePos))
            {
                movementState = SurgicalRobot.MovementState.STATE_LAUNCHING;
            }
        }
        else if (movementState == SurgicalRobot.MovementState.STATE_LAUNCHING)
        {
            if (moveTo(attachPos)) movementState = STATE_ACTIVE;
        }
        else if (movementState == STATE_ACTIVE)
        {
            if (actualTargetPos != null)
            {
                moveTo(actualTargetPos);
            }
        }
        else if (movementState == SurgicalRobot.MovementState.STATE_RETURNING)
        {
            if (moveTo(attachPos))
            {
                movementState = STATE_DOCKING;
            }
        }
        else if (movementState == STATE_DOCKING)
        {
            if (moveTo(dockingPos)) movementState = SurgicalRobot.MovementState.STATE_IDLE;
        }
    }

    private boolean blocked(BlockState state)
    {
        return state.blocksMotion();
    }

    private boolean moveTo(Vec3 toPos)
    {
        this.actualTargetPos = toPos;

        if (toPos.distanceToSqr(x, y, z) >= 0.01 * 0.01)
        {
            double dx = (toPos.x - x);
            double dy = (toPos.y - y);
            double dz = (toPos.z - z);
            if (Math.abs(dz) > 0.01f || Math.abs(dx) > 0.01f)
                this.yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double vx = dx;
            double vy = dy;
            double vz = dz;

            // Normalise motion vector if distance is greater than base movement step
            if (dist > getSpeed())
            {
                vx = vx / dist * getSpeed();
                vy = vy / dist * getSpeed();
                vz = vz / dist * getSpeed();
            }

            // Step position
            x += vx;
            y += vy;
            z += vz;

            posDirty = true;
            return false;
        }
        else return true;
    }

    public static boolean inRange(BlockPos base, Vec3i target)
    {
        float f = (float) Math.abs(target.getX() - base.getX());
        float h = (float) Math.abs(target.getZ() - base.getZ());

        if (f + h >= 128)
            return false;

        if (Math.abs(target.getY() - base.getY()) >= 512)
            return false;

        return true;
    }

    @Override
    public void setTarget(@Nullable PLC plc, @Nullable BlockPos target)
    {
        if (target != null && !inRange(basePos, target))
        {
            if (plc != null)
                plc.raiseError(new PLC.Error("position %s %s %s out of range".formatted(
                        target.getX(), target.getY(), target.getZ())));
        }
        else
        {
            if (target == null)
            {
                this.target = null;
                this.targetPos = null;
                this.actualTargetPos = null;
                building = false;
            }
            else
            {
                this.target = target.immutable();
                this.targetPos = getTarget(this.target);
                this.actualTargetPos = findTargetPos(this.targetPos);
                if (!target.equals(basePos))
                    building = true;
                else
                    building = false;
            }
        }
    }

    private Vec3 findTargetPos(Vec3 requestedTarget)
    {
        Level world = parent.getLevel();
        BlockPos posAtTarget = BlockPos.containing(requestedTarget);
        if (blocked(world.getBlockState(posAtTarget)))
        {
            var mutable = posAtTarget.mutable();
            for (Direction direction : Direction.values())
            {
                mutable.setWithOffset(posAtTarget, direction);

                if (!blocked(world.getBlockState(mutable)))
                {
                    return mutable.getCenter();
                }
            }
        }
        return requestedTarget;
    }

    private Vec3 getTarget(BlockPos target)
    {
        return Vec3.atCenterOf(target);
    }

    public double getSpeed()
    {
        return 0.2;
    }

    public Vec3 getPos()
    {
        return new Vec3(x, y, z);
    }

    public BlockPos getBlockPos()
    {
        return BlockPos.containing(x, y, z);
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double getZ()
    {
        return z;
    }

    @Override
    public boolean actuatorRemoved()
    {
        return parent.isRemoved();
    }

    private static final String NBT_MOVEMENT_STATE = "movementState";

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.putInt(NBT_MOVEMENT_STATE, movementState.ordinal());
        nbt.putDouble("rx", x);
        nbt.putDouble("ry", y);
        nbt.putDouble("rz", z);

        nbt.putFloat("yaw", yaw);

        if (stored != null)
        {
            CompoundTag storedNbt = new CompoundTag();
            storedNbt.put("item", stored.resource().toNbt());
            storedNbt.putLong("amount", stored.amount());
            nbt.put("stored", storedNbt);
        }

        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        this.movementState = values()[nbt.getInt(NBT_MOVEMENT_STATE)];
        this.x = nbt.getDouble("rx");
        this.y = nbt.getDouble("ry");
        this.z = nbt.getDouble("rz");

        this.yaw = nbt.getFloat("yaw");

        if (initial)
        {
            this.clientX = x;
            this.clientY = y;
            this.clientZ = z;
            this.clientYaw = yaw;
            initial = false;
        }

        if (nbt.contains("stored"))
        {
            CompoundTag storedNbt = nbt.getCompound("stored");
            stored = new ResourceAmount<>(
                    ItemVariant.fromNbt(storedNbt.getCompound("item")),
                    storedNbt.getLong("amount")
            );
        }
    }

    public boolean isActive()
    {
        return movementState == STATE_ACTIVE;
    }

    @Override
    public boolean reachedTarget(PLC plc)
    {
        return actualTargetPos == null || actualTargetPos.distanceToSqr(x, y, z) <= 0.1 * 0.1;
    }

    public void returnToBase()
    {
        movementState = SurgicalRobot.MovementState.STATE_RETURNING;
        setTarget(null, basePos);
    }

    public void setPos(double x, double y, double z)
    {
        // Do not accept update packets from the client when overriding the controller
        if (!parent.getLevel().isClientSide() && shouldUpdatePosition(parent.getLevel()))
            return;

        this.x = x;
        this.y = y;
        this.z = z;
        parent.setChanged();
    }

    public void stay()
    {
        this.setTarget(null, null);
    }

    public float getYaw()
    {
        return yaw;
    }

    public void setPitchYaw(float pitch, float yaw)
    {
        this.yaw = yaw;
    }

    public void spawnItem(@Nullable ResourceAmount<ItemVariant> stored)
    {
        if (stored == null)
            return;
        Containers.dropItemStack(parent.getLevel(), x, y, z, stored.resource().toStack((int) stored.amount()));
    }

    public void dumpStored(PLC plc)
    {
        if (stored != null)
        {
            spawnItem(stored);
            stored = null;
        }
    }

    @Override
    public void setStored(PLC plc, @Nullable ResourceAmount<ItemVariant> stored)
    {
        this.stored = stored;
    }

    @Override
    public BlockPos getBasePos()
    {
        return parent.getBlockPos();
    }

    @Override
    public @Nullable ResourceAmount<ItemVariant> getStored(PLC plc)
    {
        return stored;
    }

    public boolean getState()
    {
        return movementState == STATE_RETURNING;
    }

    @Override
    public void setImplantTarget(PLC plc, Vec3 implantSite, Entity target)
    {
        setTarget(plc, new BlockPos(target.getBlockX(),
                (int) Math.ceil(target.getY() + target.getBbHeight()),
                target.getBlockZ()));
    }

    public Direction getFacing()
    {
        return Direction.DOWN;
    }
}
