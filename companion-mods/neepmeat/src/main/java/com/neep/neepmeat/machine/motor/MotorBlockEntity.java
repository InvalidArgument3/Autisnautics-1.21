/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Each tick burns ethereal fuel up to {@link #MAX_POWER} absolute units.
 * Per-unit power is that energy divided by {@link com.neep.neepmeat.api.PowerUtil#BASE_POWER}.
 * Rotor speed is per-unit watts divided by the load torque, which defaults to 300.
 */

package com.neep.neepmeat.machine.motor;

import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.neepmeat.machine.NeepMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

public class MotorBlockEntity extends BlockEntity implements MotorEntity {
    /** Upstream {@code basicMotorMaxPower}. */
    public static final int MAX_POWER = 400;
    public static final int TANK_CAPACITY = 8000;

    private final LiquidFuelMachine fuel = new LiquidFuelMachine(TANK_CAPACITY);
    private float angle;
    private float power;
    private @Nullable MotorisedBlock connected;

    public MotorBlockEntity(BlockPos pos, BlockState state) {
        super(NeepMachines.MOTOR_BE.get(), pos, state);
    }

    public FluidTank tank() {
        return fuel.tank();
    }

    public void serverTick() {
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockEntity entity = level == null ? null : level.getBlockEntity(worldPosition.relative(facing));
        MotorisedBlock motorised = entity instanceof MotorisedBlock block ? block : null;
        if (connected != null && connected != motorised) {
            connected.setInputPower(0);
            connected.onMotorRemoved();
        }
        connected = motorised;
        long extracted = fuel.extractEnergy(MAX_POWER);
        power = (float) PowerUtil.absoluteToPerUnit(extracted);
        if (motorised != null) {
            motorised.setInputPower(power);
            motorised.motorTick(this);
            angle += getSpeed();
        }
        setChanged();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        onRemoved();
    }

    @Override
    public float getRotorAngle() {
        return angle;
    }

    @Override
    public float getSpeed() {
        float torque = connected == null ? PowerUtil.MOTOR_TORQUE_LOSS : connected.getLoadTorque();
        if (torque == 0) {
            torque = PowerUtil.MOTOR_TORQUE_LOSS;
        }
        return (float) (PowerUtil.perUnitToAbsWatt(power) / torque);
    }

    @Override
    public double getMechPUPower() {
        return power;
    }

    @Override
    public @Nullable MotorisedBlock getConnectedBlock() {
        return connected;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("angle", angle);
        tag.putFloat("power", power);
        fuel.save(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        angle = tag.getFloat("angle");
        power = tag.getFloat("power");
        fuel.load(tag, registries);
    }
}
