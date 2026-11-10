/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Extractor, silky extractor, service port, and reprocessor are marker components upstream.
 */

package com.neep.neepmeat.machine.live_machine.block.entity;

import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.machine.live_machine.LiveMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MarkerBlockEntity extends BlockEntity implements LivingMachineComponent {
    public MarkerBlockEntity(BlockPos pos, BlockState state) {
        super(LiveMachines.MARKER_BE.get(), pos, state);
    }

    @Override
    public boolean componentRemoved() {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> componentType() {
        if (getBlockState().is(LiveMachines.SILKY_EXTRACTOR.get())) {
            return com.neep.neepmeat.machine.live_machine.LivingMachineComponents.SILKY_EXTRACTOR;
        }
        if (getBlockState().is(LiveMachines.SERVICE_PORT.get())) {
            return com.neep.neepmeat.machine.live_machine.LivingMachineComponents.SERVICE_PORT;
        }
        return com.neep.neepmeat.machine.live_machine.LivingMachineComponents.EXTRACTOR;
    }
}
