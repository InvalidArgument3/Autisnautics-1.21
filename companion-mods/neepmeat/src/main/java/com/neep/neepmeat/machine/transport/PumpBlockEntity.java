/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Upstream pump tick moves FluidTransportConstants.maxTransfer from the rear
 * storage into the front storage. Here that amount is 125 mB, simulated first.
 */

package com.neep.neepmeat.machine.transport;

import com.neep.neepmeat.machine.NeepMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class PumpBlockEntity extends BlockEntity {
    private int cooldown;

    public PumpBlockEntity(BlockPos pos, BlockState state) {
        super(NeepMachines.PUMP_BE.get(), pos, state);
    }

    public void serverTick() {
        if (level == null || cooldown-- > 0) {
            return;
        }
        cooldown = 10;
        Direction facing = getBlockState().getValue(PumpBlock.FACING);
        IFluidHandler rear = level.getCapability(Capabilities.FluidHandler.BLOCK, worldPosition.relative(facing.getOpposite()), facing);
        boolean moved = false;
        if (rear != null) {
            FluidStack simulated = rear.drain(FluidPipeBlockEntity.THROUGHPUT_MB, IFluidHandler.FluidAction.SIMULATE);
            if (!simulated.isEmpty() && push(facing, simulated)) {
                rear.drain(simulated, IFluidHandler.FluidAction.EXECUTE);
                moved = true;
            }
        }
        if (getBlockState().getValue(PumpBlock.ACTIVE) != moved) {
            level.setBlock(worldPosition, getBlockState().setValue(PumpBlock.ACTIVE, moved), net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }
    }

    private boolean push(Direction facing, FluidStack stack) {
        BlockPos front = worldPosition.relative(facing);
        if (level.getBlockEntity(front) instanceof FluidPipeBlockEntity pipe) {
            return pipe.insert(stack, facing.getOpposite()) == stack.getAmount();
        }
        IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, front, facing.getOpposite());
        if (handler == null) {
            return false;
        }
        if (handler.fill(stack, IFluidHandler.FluidAction.SIMULATE) < stack.getAmount()) {
            return false;
        }
        return handler.fill(stack, IFluidHandler.FluidAction.EXECUTE) == stack.getAmount();
    }
}
