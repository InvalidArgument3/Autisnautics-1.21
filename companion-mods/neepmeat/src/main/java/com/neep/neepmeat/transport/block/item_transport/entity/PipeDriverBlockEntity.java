/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Holds deferred requests and resolves them: SUCCESS when the destination has
 * the stack, PASS while a pipe is carrying it or a crafter still owes it, FAIL
 * after the wait.
 */

package com.neep.neepmeat.transport.block.item_transport.entity;
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
import com.neep.neepmeat.transport.block.fluid_transport.machine.PumpBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.FilterItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.ItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.MergePipeBlock;
import com.neep.neepmeat.transport.block.item_transport.PipeDriverBlock;
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.item_network.PipeRouteFinder;

import com.neep.neepmeat.machine.NeepMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PipeDriverBlockEntity extends BlockEntity {
    private final List<DeferredRequest> deferred = new ArrayList<>();
    private int cooldown;

    public PipeDriverBlockEntity(BlockPos pos, BlockState state) {
        super(NeepMachines.PIPE_DRIVER_BE.get(), pos, state);
    }

    public void defer(BlockPos destination, ItemStack stack) {
        for (DeferredRequest request : deferred) {
            if (request.destination().equals(destination) && ItemStack.isSameItem(request.stack(), stack) && request.state() == DeferredRequest.State.PASS) {
                return;
            }
        }
        deferred.add(new DeferredRequest(destination, stack));
        setChanged();
    }

    public DeferredRequest.State stateFor(BlockPos destination) {
        for (DeferredRequest request : deferred) {
            if (request.destination().equals(destination)) {
                return request.state();
            }
        }
        return null;
    }

    public void serverTick() {
        if (level == null) {
            return;
        }
        boolean valid = false;
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(worldPosition.relative(direction)).getBlock() instanceof ItemPipeBlock) {
                valid = true;
                break;
            }
        }
        if (getBlockState().getValue(PipeDriverBlock.VALID) != valid) {
            level.setBlock(worldPosition, getBlockState().setValue(PipeDriverBlock.VALID, valid), Block.UPDATE_CLIENTS);
        }
        if (cooldown-- > 0) {
            return;
        }
        cooldown = 10;
        Iterator<DeferredRequest> iterator = deferred.iterator();
        while (iterator.hasNext()) {
            DeferredRequest request = iterator.next();
            if (request.state() != DeferredRequest.State.PASS) {
                continue;
            }
            if (destinationHas(request)) {
                request.succeed();
                continue;
            }
            RoutingNetwork.deliverCrafted(level, request.destination(), request.stack());
            if (destinationHas(request) ) {
                request.succeed();
                continue;
            }
            if (RoutingNetwork.inTransit(level, worldPosition, request.stack())) {
                continue;
            }
            request.tick();
            if (request.age() > 8) {
                request.fail();
            }
        }
        setChanged();
    }

    private boolean destinationHas(DeferredRequest request) {
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, request.destination(), null);
        if (handler == null && level.getBlockEntity(request.destination()) instanceof IItemHandler direct) {
            handler = direct;
        }
        if (handler == null) {
            return false;
        }
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (ItemStack.isSameItem(handler.getStackInSlot(slot), request.stack())) {
                return true;
            }
        }
        return false;
    }
}
