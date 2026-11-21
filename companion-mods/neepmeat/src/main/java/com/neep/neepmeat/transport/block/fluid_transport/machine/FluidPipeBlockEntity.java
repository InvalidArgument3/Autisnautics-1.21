/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Throughput is the upstream basic pipe value: 10125 fabric droplets, which is
 * 125 mB. A pipe holds one packet and pushes it out the next connected side.
 * Pumps and the fluid vertex graph are not ported, so nothing enters a pipe
 * unless something calls {@link #insert}.
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
import com.neep.neepmeat.transport.block.fluid_transport.machine.PumpBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.FilterItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.ItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.MergePipeBlock;
import com.neep.neepmeat.transport.block.item_transport.PipeDriverBlock;
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity;
import com.neep.neepmeat.transport.item_network.PipeRouteFinder;

import com.neep.neepmeat.machine.NeepMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class FluidPipeBlockEntity<V> extends BlockEntity {
    /** {@code data/neepmeat/transport/fluid_transport.json} basic_pipe_throughput, converted from droplets. */
    public static final int THROUGHPUT_MB = 125;

    private FluidStack held = FluidStack.EMPTY;
    private Direction in = Direction.NORTH;
    private int lastOutput;

    public FluidPipeBlockEntity(BlockPos pos, BlockState state) {
        super(NeepMachines.FLUID_PIPE_BE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide && getBlockState().getBlock() instanceof ConnectedPipeBlock pipe) {
            pipe.refreshConnections(level, worldPosition);
        }
    }

    public int insert(FluidStack stack, Direction from) {
        if (stack.isEmpty() || !held.isEmpty() || level == null) {
            return 0;
        }
        Direction out = chooseOutput(from);
        if (out == null) {
            return 0;
        }
        int amount = Math.min(THROUGHPUT_MB, stack.getAmount());
        held = stack.copy();
        held.setAmount(amount);
        in = from;
        lastOutput = out.ordinal();
        setChanged();
        return amount;
    }

    public void serverTick() {
        Level level = getLevel();
        if (level == null || held.isEmpty()) {
            return;
        }
        Direction out = Direction.values()[lastOutput];
        BlockPos target = worldPosition.relative(out);
        BlockState state = level.getBlockState(target);
        if (state.getBlock() instanceof FluidPipeBlock && level.getBlockEntity(target) instanceof FluidPipeBlockEntity next) {
            int moved = next.insert(held, out.getOpposite());
            held.shrink(moved);
        } else {
            IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, target, out.getOpposite());
            if (handler != null) {
                int moved = handler.fill(held.copy(), IFluidHandler.FluidAction.EXECUTE);
                held.shrink(moved);
            }
        }
        if (held.getAmount() <= 0) {
            held = FluidStack.EMPTY;
        }
        setChanged();
    }

    public int heldAmount() {
        return held.getAmount();
    }

    private Direction chooseOutput(Direction from) {
        for (Direction direction : Direction.values()) {
            if (direction == from || level == null) {
                continue;
            }
            BlockPos neighbour = worldPosition.relative(direction);
            BlockState state = level.getBlockState(neighbour);
            if (state.getBlock() instanceof FluidPipeBlock other && colorsMatch(other)) {
                return direction;
            }
            if (level.getCapability(Capabilities.FluidHandler.BLOCK, neighbour, direction.getOpposite()) != null) {
                return direction;
            }
        }
        return null;
    }

    private boolean colorsMatch(FluidPipeBlock other) {
        if (!(getBlockState().getBlock() instanceof FluidPipeBlock self)) {
            return true;
        }
        return self.color() == null || other.color() == null || self.color() == other.color();
    }
}
