/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Stores compressed-air droplets. Upstream inserts them into a fluid storage.
 */

package com.neep.neepmeat.machine.live_machine.block.entity;

import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.machine.live_machine.LiveMachines;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LargeCompressorBlockEntity extends BlockEntity implements LivingMachineComponent {
    public static final int POWER_TO_DROPLETS = 400;
    private long droplets;

    public LargeCompressorBlockEntity(BlockPos pos, BlockState state) {
        super(LiveMachines.COMPRESSOR_BE.get(), pos, state);
    }

    public long droplets() {
        return droplets;
    }

    public void insertDroplets(long amount) {
        droplets += amount;
    }

    @Override
    public boolean componentRemoved() {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> componentType() {
        return LivingMachineComponents.LARGE_COMPRESSOR;
    }
}
