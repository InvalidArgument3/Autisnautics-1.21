/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Active when redstone and activeWithoutRedstone differ, matching updatePowered.
 * While the back mode is PULL and the front mode is PUSH, fluid moves through a
 * 1000 mB buffer at the basic pipe throughput. Simulate runs before execute.
 */

package com.neep.neepmeat.transport.block.fluid_transport.machine;
import com.neep.neepmeat.machine.transport.ConnectedPipeBlock;
import com.neep.neepmeat.machine.transport.DeferredRequest;
import com.neep.neepmeat.machine.transport.FilterEntry;
import com.neep.neepmeat.machine.transport.FilterPipeMenu;
import com.neep.neepmeat.machine.transport.NetworkCrafterBlockEntity;
import com.neep.neepmeat.machine.transport.StorageBusBlock;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.block.fluid_transport.FluidPipeBlock;
import com.neep.neepmeat.transport.block.fluid_transport.PumpBlock;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidPipeBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.FilterItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.ItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.MergePipeBlock;
import com.neep.neepmeat.transport.block.item_transport.PipeDriverBlock;
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity;
import com.neep.neepmeat.transport.item_network.PipeRouteFinder;

import com.neep.neepmeat.machine.NeepMachines;
import com.neep.neepmeat.transport.AcceptorModes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class PumpBlockEntity extends BlockEntity {
    public static final int BUFFER_MB = 1000;
    private final FluidTank buffer = new FluidTank(BUFFER_MB);
    private boolean activeWithoutRedstone;
    private AcceptorModes frontMode = AcceptorModes.NONE;
    private AcceptorModes backMode = AcceptorModes.NONE;
    private int cooldown;

    public PumpBlockEntity(BlockPos pos, BlockState state) {
        super(NeepMachines.PUMP_BE.get(), pos, state);
    }

    public void changeMode() {
        activeWithoutRedstone = !activeWithoutRedstone;
        updatePowered(level != null && level.hasNeighborSignal(worldPosition));
    }

    public void updatePowered(boolean powered) {
        boolean active = powered != activeWithoutRedstone;
        if (level != null && getBlockState().getValue(PumpBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, getBlockState().setValue(PumpBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
        }
        if (active) {
            frontMode = AcceptorModes.PUSH;
            backMode = AcceptorModes.PULL;
        } else {
            frontMode = AcceptorModes.NONE;
            backMode = AcceptorModes.NONE;
        }
        setChanged();
    }

    public int bufferAmount() {
        return buffer.getFluidAmount();
    }

    public void serverTick() {
        if (level == null || cooldown-- > 0 || !backMode.isDriving()) {
            return;
        }
        cooldown = 10;
        Direction facing = getBlockState().getValue(PumpBlock.FACING);
        IFluidHandler rear = level.getCapability(Capabilities.FluidHandler.BLOCK, worldPosition.relative(facing.getOpposite()), facing);
        if (rear != null && buffer.getFluidAmount() < BUFFER_MB) {
            FluidStack simulated = rear.drain(FluidPipeBlockEntity.THROUGHPUT_MB, IFluidHandler.FluidAction.SIMULATE);
            int accepted = buffer.fill(simulated, IFluidHandler.FluidAction.SIMULATE);
            if (accepted > 0) {
                FluidStack drained = rear.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
                buffer.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            }
        }
        if (frontMode.isDriving() && !buffer.isEmpty()) {
            FluidStack packet = buffer.drain(FluidPipeBlockEntity.THROUGHPUT_MB, IFluidHandler.FluidAction.SIMULATE);
            if (push(facing, packet)) {
                buffer.drain(packet, IFluidHandler.FluidAction.EXECUTE);
            }
        }
        setChanged();
    }

    private boolean push(Direction facing, FluidStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        BlockPos front = worldPosition.relative(facing);
        if (level.getBlockEntity(front) instanceof FluidPipeBlockEntity pipe) {
            return pipe.insert(stack, facing.getOpposite()) == stack.getAmount();
        }
        IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, front, facing.getOpposite());
        if (handler == null || handler.fill(stack, IFluidHandler.FluidAction.SIMULATE) < stack.getAmount()) {
            return false;
        }
        return handler.fill(stack, IFluidHandler.FluidAction.EXECUTE) == stack.getAmount();
    }
}
