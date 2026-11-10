/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.machine.transport;

import com.neep.neepmeat.machine.NeepMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class FluidPipeBlockEntity extends BlockEntity {
    private static final int RATE = 250;
    private FluidStack buffer = FluidStack.EMPTY;
    private int cooldown;

    public FluidPipeBlockEntity(BlockPos pos, BlockState state) {
        super(NeepMachines.FLUID_PIPE_BE.get(), pos, state);
    }

    public void serverTick() {
        Level level = getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        if (cooldown-- > 0) {
            return;
        }
        cooldown = 5;
        Direction facing = getBlockState().getValue(FluidPipeBlock.FACING);
        if (!buffer.isEmpty()) {
            IFluidHandler destination = level.getCapability(Capabilities.FluidHandler.BLOCK, worldPosition.relative(facing), facing.getOpposite());
            if (destination != null) {
                int filled = destination.fill(buffer.copy(), IFluidHandler.FluidAction.EXECUTE);
                buffer.shrink(filled);
                if (buffer.getAmount() <= 0) {
                    buffer = FluidStack.EMPTY;
                }
                setChanged();
            }
            return;
        }
        IFluidHandler source = level.getCapability(Capabilities.FluidHandler.BLOCK, worldPosition.relative(facing.getOpposite()), facing);
        if (source == null) {
            return;
        }
        FluidStack drained = source.drain(RATE, IFluidHandler.FluidAction.EXECUTE);
        if (!drained.isEmpty()) {
            buffer = drained;
            setChanged();
        }
    }
}
