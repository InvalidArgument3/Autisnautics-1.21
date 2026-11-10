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

package com.neep.neepmeat.plc.arm;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.machine.motor.MotorEntity;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.plc.block.entity.CombineActuator;
import com.neep.neepmeat.plc.instruction.PlcInstruction;
import com.neep.neepmeat.plc.robot.PLCActuator;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RoboticArmBlockEntity extends SyncableBlockEntity implements CombineActuator, PLCActuator.Provider, MotorisedBlock, MotorisedBlock.DiagnosticsProvider
{
    private float power;

    private @Nullable BlockPos target;
    private final float minPower = 0.05f;

    private double tipX = getBlockPos().getX() + 1;
    private double tipY = getBlockPos().getY() + 1;
    private double tipZ = getBlockPos().getZ();

    public double prevX = tipX;
    public double prevY = tipY;
    public double prevZ = tipZ;

    private final double range = 4.5;

    private Vec3 targetVec = new Vec3(tipX, tipY, tipZ);
    @Nullable
    private ResourceAmount<ItemVariant> stored;

    public RoboticArmBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putDouble("rx", tipX);
        nbt.putDouble("ry", tipY);
        nbt.putDouble("rz", tipZ);

        nbt.putDouble("tx", targetVec.x);
        nbt.putDouble("ty", targetVec.y);
        nbt.putDouble("tz", targetVec.z);

        nbt.putFloat("power", power);

        nbt.put("stored", PlcInstruction.writeItem(stored));

        if (target != null)
            nbt.put("target", NbtUtils.writeBlockPos(target));
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.tipX = nbt.getDouble("rx");
        this.tipY = nbt.getDouble("ry");
        this.tipZ = nbt.getDouble("rz");

        this.power = nbt.getFloat("power");

        this.targetVec = new Vec3(
                nbt.getDouble("tx"),
                nbt.getDouble("ty"),
                nbt.getDouble("tz")
        );

        this.stored = PlcInstruction.readItem(nbt.getCompound("stored"));

        if (nbt.contains("target"))
            this.target = NbtUtils.readBlockPos(nbt.getCompound("target"));
    }

    public void serverTick()
    {
        prevX = tipX;
        prevY = tipY;
        prevZ = tipZ;

//        if (targetVec != null)
//        {
//            double distance = targetVec.distanceTo(new Vec3d(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ()));
//            if (distance < range)
//        }

        moveTo(targetVec);
    }

    public void clientTick()
    {
//        if (targetVec != null)
//        {
//            double distance = targetVec.distanceTo(new Vec3d(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ()));
//            if (distance < range)
//        }
        moveTo(targetVec);
    }

    private void moveTo(Vec3 toPos)
    {
        if (!reachedTarget(null))
        {

            double dx = (toPos.x - tipX);
            double dy = (toPos.y - tipY);
            double dz = (toPos.z - tipZ);
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

            if (origin().distanceToSqr(tipX + vx, tipY + vy, tipZ + vz) >= range * range)
                return;

            // Step position
            tipX += vx;
            tipY += vy;
            tipZ += vz;
        }
    }

    private Vec3 origin()
    {
        return new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5);
    }

    private double getSpeed()
    {
        if (power < minPower)
            return 0;

        return Mth.clamp(power, 0, 1.5);
    }

    @Override
    public void setTarget(PLC plc, @Nullable BlockPos target) throws NeepASM.RuntimeException
    {
        if (power < minPower)
        {
            throw new NeepASM.RuntimeException("Insufficient power");
        }

        this.target = target;
        if (target != null)
            this.targetVec = Vec3.upFromBottomCenterOf(target, 0.0);
        else
            this.targetVec = new Vec3(tipX, tipY, tipZ);

        sync();
    }

    @Override
    public boolean reachedTarget(@Nullable PLC plc)
    {
        return target == null || targetVec.distanceToSqr(tipX, tipY,tipZ) <= 0.1 * 0.1;
    }

    @Override
    public void spawnItem(@Nullable ResourceAmount<ItemVariant> stored)
    {
        if (stored == null)
            return;
        Containers.dropItemStack(getLevel(), tipX, tipY, tipZ, stored.resource().toStack((int) stored.amount()));
    }

    @Override
    public void dumpStored(PLC plc)
    {
        if (stored != null)
        {
            spawnItem(stored);
            stored = null;
            setChanged();
        }
    }

    @Override
    public void setStored(PLC plc, @Nullable ResourceAmount<ItemVariant> stored)
    {
        this.stored = stored;
        setChanged();
    }

    @Override
    public BlockPos getBasePos()
    {
        return worldPosition;
    }

    @Override
    public @Nullable ResourceAmount<ItemVariant> getStored(PLC plc)
    {
        return stored;
    }

    @Override
    public double getX() { return tipX; }

    @Override
    public double getY() { return tipY; }

    @Override
    public double getZ() { return tipZ; }

    @Override
    public boolean actuatorRemoved()
    {
        return isRemoved();
    }

//    public Vec3d getTarget(float tickDelta)
//    {
//        return new Vec3d(
//                MathHelper.lerp(tickDelta, prevX, tipX),
//                MathHelper.lerp(tickDelta, prevY, tipY),
//                MathHelper.lerp(tickDelta, prevZ, tipZ)
//        );
//    }

    @Override
    public boolean motorTick(MotorEntity motor)
    {
        return false;
    }

    @Override
    public void setInputPower(float power)
    {
        this.power = power;
        sync();
    }

    @Override
    public PLCActuator getPlcActuator()
    {
        return this;
    }

    @Override
    public Diagnostics getDiagnostics()
    {
        return MotorisedBlock.Diagnostics.insufficientPower(power <= minPower, power, minPower);
    }
}
