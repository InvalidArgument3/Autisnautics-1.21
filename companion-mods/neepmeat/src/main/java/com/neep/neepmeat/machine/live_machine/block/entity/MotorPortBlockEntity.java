/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.machine.live_machine.block.entity;

import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.motor.MotorEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MotorPortBlockEntity extends BlockEntity implements LivingMachineComponent, MotorisedBlock {
    private @Nullable BlockPos controller;
    private float inputPower;

    public MotorPortBlockEntity(BlockPos pos, BlockState state) {
        super(LivingMachines.MOTOR_PORT_BE.get(), pos, state);
    }

    @Override
    public void setController(@Nullable BlockPos pos) {
        this.controller = pos;
    }

    public @Nullable BlockPos controller() {
        return controller;
    }

    public float getInputSignal() {
        return inputPower;
    }

    @Override
    public boolean motorTick(MotorEntity motor) {
        return false;
    }

    @Override
    public void setInputPower(float power) {
        this.inputPower = power;
    }

    @Override
    public boolean componentRemoved() {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType() {
        return LivingMachineComponents.MOTOR_PORT;
    }
}
