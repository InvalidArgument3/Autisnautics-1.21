/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.machine.live_machine;

import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MotorPortBlockEntity extends BlockEntity implements LivingMachineComponent {
    private @Nullable BlockPos controller;

    public MotorPortBlockEntity(BlockPos pos, BlockState state) {
        super(LiveMachines.MOTOR_PORT_BE.get(), pos, state);
    }

    @Override
    public void setController(@Nullable BlockPos pos) {
        this.controller = pos;
    }

    public @Nullable BlockPos controller() {
        return controller;
    }

    @Override
    public boolean componentRemoved() {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> componentType() {
        return LivingMachineComponents.MOTOR_PORT;
    }
}
