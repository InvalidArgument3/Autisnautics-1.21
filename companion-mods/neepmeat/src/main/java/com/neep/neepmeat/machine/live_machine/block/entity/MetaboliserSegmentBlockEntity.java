/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Single block. Upstream's segment is a 3x3. It is a blood source that emits the stored energy once.
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

public class MetaboliserSegmentBlockEntity extends BlockEntity implements LivingMachineComponent, BloodAcceptor {
    private long energy;

    public MetaboliserSegmentBlockEntity(BlockPos pos, BlockState state) {
        super(LivingMachines.METABOLISER_BE.get(), pos, state);
    }

    public long energy() {
        return energy;
    }

    public void setOutputEnergy(long amount) {
        this.energy = amount;
    }

    @Override
    public long output() {
        long produced = energy;
        energy = 0;
        return produced;
    }

    @Override
    public Mode mode() {
        return Mode.SOURCE;
    }

    @Override
    public boolean componentRemoved() {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType() {
        return LivingMachineComponents.METABOLISER_SEGMENT;
    }
}
