/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.plc.arm;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.plc.block.entity.CombineActuator;
import com.neep.neepmeat.plc.robot.PLCActuator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RoboticArmBlockEntity extends SyncableBlockEntity implements CombineActuator, PLCActuator.Provider, MotorisedBlock, MotorisedBlock.DiagnosticsProvider {
    private float power;
    private @Nullable BlockPos target;
    private final float minPower = 0.05f;

    private double tipX;
    private double tipY;
    private double tipZ;

    public double prevX;
    public double prevY;
    public double prevZ;

    private final double range = 4.5;
    private Vec3 targetVec;
    private ItemStack stored = ItemStack.EMPTY;

    public RoboticArmBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.tipX = pos.getX() + 1;
        this.tipY = pos.getY() + 1;
        this.tipZ = pos.getZ();
        this.prevX = tipX;
        this.prevY = tipY;
        this.prevZ = tipZ;
        this.targetVec = new Vec3(tipX, tipY, tipZ);
    }

    public RoboticArmBlockEntity(BlockPos pos, BlockState state) {
        this(com.neep.neepmeat.machine.NeepMachines.ROBOTIC_ARM_BE.get(), pos, state);
    }

    @Override
    protected void writeSave(CompoundTag nbt, HolderLookup.Provider registries) {
        nbt.putDouble("rx", tipX);
        nbt.putDouble("ry", tipY);
        nbt.putDouble("rz", tipZ);
        nbt.putDouble("tx", targetVec.x);
        nbt.putDouble("ty", targetVec.y);
        nbt.putDouble("tz", targetVec.z);
        nbt.putFloat("power", power);
        if (!stored.isEmpty()) {
            nbt.put("stored", stored.save(registries));
        }
        if (target != null) {
            nbt.put("target", NbtUtils.writeBlockPos(target));
        }
    }

    @Override
    protected void readSave(CompoundTag nbt, HolderLookup.Provider registries) {
        this.tipX = nbt.getDouble("rx");
        this.tipY = nbt.getDouble("ry");
        this.tipZ = nbt.getDouble("rz");
        this.power = nbt.getFloat("power");
        this.targetVec = new Vec3(nbt.getDouble("tx"), nbt.getDouble("ty"), nbt.getDouble("tz"));
        this.stored = ItemStack.EMPTY;
        if (nbt.contains("stored", Tag.TAG_COMPOUND)) {
            this.stored = ItemStack.parse(registries, nbt.getCompound("stored")).orElse(ItemStack.EMPTY);
        }
        this.target = nbt.contains("target") ? NbtUtils.readBlockPos(nbt, "target").orElse(null) : null;
        this.prevX = tipX;
        this.prevY = tipY;
        this.prevZ = tipZ;
    }

    public void serverTick() {
        prevX = tipX;
        prevY = tipY;
        prevZ = tipZ;
        setWantedPosition(targetVec);
    }

    public void clientTick() {
        setWantedPosition(targetVec);
    }

    private void setWantedPosition(Vec3 toPos) {
        if (reachedTarget(null)) {
            return;
        }
        double dx = toPos.x - tipX;
        double dy = toPos.y - tipY;
        double dz = toPos.z - tipZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double vx = dx;
        double vy = dy;
        double vz = dz;
        if (dist > getSpeed()) {
            vx = vx / dist * getSpeed();
            vy = vy / dist * getSpeed();
            vz = vz / dist * getSpeed();
        }
        if (origin().distanceToSqr(tipX + vx, tipY + vy, tipZ + vz) >= range * range) {
            return;
        }
        tipX += vx;
        tipY += vy;
        tipZ += vz;
    }

    private Vec3 origin() {
        BlockPos pos = getBlockPos();
        return new Vec3(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
    }

    private double getSpeed() {
        if (power < minPower) {
            return 0;
        }
        return Mth.clamp(power, 0, 1.5);
    }

    @Override
    public void setTarget(PLC plc, @Nullable BlockPos target) throws NeepASM.RuntimeException {
        if (power < minPower) {
            throw new NeepASM.RuntimeException("Insufficient power");
        }
        this.target = target;
        if (target != null) {
            this.targetVec = Vec3.upFromBottomCenterOf(target, 0.0);
        } else {
            this.targetVec = new Vec3(tipX, tipY, tipZ);
        }
        sync();
    }

    @Override
    public boolean reachedTarget(@Nullable PLC plc) {
        return target == null || targetVec.distanceToSqr(tipX, tipY, tipZ) <= 0.1 * 0.1;
    }

    @Override
    public void spawnItem(ItemStack stack) {
        if (stack == null || stack.isEmpty() || getLevel() == null) {
            return;
        }
        Containers.dropItemStack(getLevel(), tipX, tipY, tipZ, stack.copy());
    }

    @Override
    public void dumpStored(PLC plc) {
        if (!stored.isEmpty()) {
            spawnItem(stored);
            stored = ItemStack.EMPTY;
            setChanged();
        }
    }

    @Override
    public void setStored(PLC plc, ItemStack stored) {
        this.stored = stored == null ? ItemStack.EMPTY : stored.copy();
        setChanged();
    }

    @Override
    public BlockPos getBasePos() {
        return getBlockPos();
    }

    @Override
    public ItemStack getStored(PLC plc) {
        return stored;
    }

    @Override
    public double getX() {
        return tipX;
    }

    @Override
    public double getY() {
        return tipY;
    }

    @Override
    public double getZ() {
        return tipZ;
    }

    @Override
    public boolean actuatorRemoved() {
        return isRemoved();
    }

    @Override
    public boolean motorTick(com.neep.neepmeat.machine.motor.MotorEntity motor) {
        return false;
    }

    @Override
    public void setInputPower(float power) {
        this.power = power;
        sync();
    }

    public float power() {
        return power;
    }

    @Override
    public PLCActuator getPlcActuator() {
        return this;
    }

    @Override
    public Diagnostics getDiagnostics() {
        return MotorisedBlock.Diagnostics.insufficientPower(power <= minPower, power, minPower);
    }
}
