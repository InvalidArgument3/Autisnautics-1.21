/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * A passive sink. An offer of at least 0.05 per unit is stored as 0.05 and that amount is consumed.
 */

package com.neep.neepmeat.machine.live_machine.block.entity;

import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LuckyOneBlockEntity extends BlockEntity implements LivingMachineComponent, BloodAcceptor {
    private float influx;

    public LuckyOneBlockEntity(BlockPos pos, BlockState state) {
        super(LivingMachines.LUCKY_ONE_BE.get(), pos, state);
    }

    public float influx() {
        return influx;
    }

    @Override
    public float updateInflux(float offered) {
        if (offered >= 0.05f) {
            influx = 0.05f;
            return 0.05f;
        }
        influx = 0;
        return 0;
    }

    @Override
    public Mode mode() {
        return Mode.SINK;
    }

    @Override
    public boolean componentRemoved() {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType() {
        return LivingMachineComponents.LUCKY_ONE;
    }
}
