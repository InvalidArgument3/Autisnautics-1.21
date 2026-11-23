/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * One block. Upstream is a multiblock. The tank is a droplet storage of one bucket.
 */

package com.neep.neepmeat.machine.live_machine.block.entity;

import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LargeCompressorBlockEntity extends BlockEntity implements LivingMachineComponent {
    public static final int POWER_TO_DROPLETS = 400;
    private final WritableSingleFluidStorage output = new WritableSingleFluidStorage(FluidConstants.BUCKET, this::setChanged);

    public LargeCompressorBlockEntity(BlockPos pos, BlockState state) {
        super(LivingMachines.COMPRESSOR_BE.get(), pos, state);
    }

    public Storage<FluidVariant> output() {
        return output;
    }

    public long droplets() {
        return output.amount;
    }

    public FluidVariant fluid() {
        return output.variant;
    }

    @Override
    public boolean componentRemoved() {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType() {
        return LivingMachineComponents.LARGE_COMPRESSOR;
    }
}
