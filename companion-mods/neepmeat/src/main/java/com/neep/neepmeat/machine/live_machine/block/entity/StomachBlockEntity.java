/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Single block. Upstream's stomach is a big block. The fluid is stored in droplets.
 */

package com.neep.neepmeat.machine.live_machine.block.entity;

import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.machine.live_machine.LiveMachines;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class StomachBlockEntity extends BlockEntity implements LivingMachineComponent {
    private Fluid fluid = Fluids.EMPTY;
    private long droplets;

    public StomachBlockEntity(BlockPos pos, BlockState state) {
        super(LiveMachines.STOMACH_BE.get(), pos, state);
    }

    public Fluid fluid() {
        return fluid;
    }

    public void store(Fluid fluid, long droplets) {
        this.fluid = fluid;
        this.droplets = droplets;
    }

    public long extractDroplets(long amount) {
        long taken = Math.min(droplets, amount);
        droplets -= taken;
        if (droplets == 0) {
            fluid = Fluids.EMPTY;
        }
        return taken;
    }

    public long droplets() {
        return droplets;
    }

    @Override
    public boolean componentRemoved() {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> componentType() {
        return LivingMachineComponents.STOMACH;
    }
}
