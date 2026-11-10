/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fuel extraction from LiquidFuelMachine is not in this block. While running it
 * supplies one mechanical unit to the machine on its facing side.
 */

package com.neep.neepmeat.machine.motor;

import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.neepmeat.machine.NeepMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public class MotorBlockEntity extends BlockEntity implements MotorEntity {
    public static final float OUTPUT = 1.0F;
    private float angle;
    private @Nullable MotorisedBlock connected;

    public MotorBlockEntity(BlockPos pos, BlockState state) {
        super(NeepMachines.MOTOR_BE.get(), pos, state);
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
        if (motorised != null) {
            motorised.setInputPower(OUTPUT);
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
        return connected == null ? 0 : OUTPUT;
    }

    @Override
    public double getMechPUPower() {
        return connected == null ? 0 : OUTPUT;
    }

    @Override
    public @Nullable MotorisedBlock getConnectedBlock() {
        return connected;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("angle", angle);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        angle = tag.getFloat("angle");
    }
}
