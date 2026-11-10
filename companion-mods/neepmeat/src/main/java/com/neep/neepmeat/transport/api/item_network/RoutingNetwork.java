/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Supply, crafting recursion (depth 30), and deferred callbacks. A request that
 * cannot be filled from storage recurses into a network crafter's ingredients.
 * The driver keeps the request until the destination holds the stack.
 */

package com.neep.neepmeat.transport.api.item_network;
import com.neep.neepmeat.machine.transport.ConnectedPipeBlock;
import com.neep.neepmeat.machine.transport.DeferredRequest;
import com.neep.neepmeat.machine.transport.FilterEntry;
import com.neep.neepmeat.machine.transport.FilterPipeMenu;
import com.neep.neepmeat.machine.transport.NetworkCrafterBlockEntity;
import com.neep.neepmeat.machine.transport.StorageBusBlock;
import com.neep.neepmeat.transport.block.fluid_transport.FluidPipeBlock;
import com.neep.neepmeat.transport.block.fluid_transport.PumpBlock;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidPipeBlockEntity;
import com.neep.neepmeat.transport.block.fluid_transport.machine.PumpBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.FilterItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.ItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.MergePipeBlock;
import com.neep.neepmeat.transport.block.item_transport.PipeDriverBlock;
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity;
import com.neep.neepmeat.transport.item_network.PipeRouteFinder;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.transport.ItemRoute;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public final class RoutingNetwork {
    public static final int MAX_DEPTH = 30;
    public static final BlockApiLookup<RoutingNetwork, Void> LOOKUP = BlockApiLookup.get(
            ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "routing_network"), RoutingNetwork.class, Void.class);

    private RoutingNetwork() {}

    public static boolean request(Level level, BlockPos destination, ItemStack wanted) {
        boolean started = request(level, destination, wanted, 0);
        if (started) {
            PipeDriverBlockEntity driver = findDriver(level, destination);
            if (driver != null) {
                driver.defer(destination, wanted);
            }
        }
        return started;
    }

    private static boolean request(Level level, BlockPos destination, ItemStack wanted, int depth) {
        if (wanted.isEmpty() || depth > MAX_DEPTH) {
            return false;
        }
        if (supply(level, destination, wanted)) {
            return true;
        }
        NetworkCrafterBlockEntity crafter = findCrafter(level, destination, wanted);
        if (crafter == null) {
            return false;
        }
        for (var ingredient : crafter.ingredients()) {
            if (crafter.has(ingredient)) {
                continue;
            }
            if (!request(level, crafter.getBlockPos(), new ItemStack(ingredient), depth + 1)) {
                return false;
            }
        }
        return true;
    }

    public static void finishCrafts(Level level) {
        // Crafted outputs are routed by the driver once the crafter tick has filled them.
    }

    public static boolean deliverCrafted(Level level, BlockPos destination, ItemStack wanted) {
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(destination);
        seen.add(destination);
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (!seen.add(next)) {
                    continue;
                }
                if (level.getBlockEntity(next) instanceof NetworkCrafterBlockEntity crafter
                        && ItemStack.isSameItem(crafter.crafted(), wanted)) {
                    ItemStack stack = crafter.extractItem(4, 1, true);
                    BlockPos pipePos = adjacentPipe(level, next);
                    if (pipePos == null || !(level.getBlockEntity(pipePos) instanceof ItemPipeBlockEntity pipe)) {
                        return false;
                    }
                    Direction fromCrafter = directionBetween(next, pipePos);
                    ItemRoute route = PipeRouteFinder.find(level, pipePos, fromCrafter.getOpposite(), stack);
                    if (route.isEmpty()) {
                        return false;
                    }
                    if (pipe.insert(stack, fromCrafter.getOpposite(), route) == stack.getCount()) {
                        crafter.extractItem(4, 1, false);
                        return true;
                    }
                    return false;
                }
                if (level.getBlockEntity(next) instanceof ItemPipeBlockEntity) {
                    queue.add(next);
                }
            }
        }
        return false;
    }

    public static boolean inTransit(Level level, BlockPos origin, ItemStack wanted) {
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);
        seen.add(origin);
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (!seen.add(next) || !(level.getBlockEntity(next) instanceof ItemPipeBlockEntity pipe)) {
                    continue;
                }
                queue.add(next);
                if (pipe.carrying(wanted)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean supply(Level level, BlockPos destination, ItemStack wanted) {
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(destination);
        seen.add(destination);
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (!seen.add(next) || !(level.getBlockEntity(next) instanceof ItemPipeBlockEntity pipe)) {
                    continue;
                }
                queue.add(next);
                if (pipe.getBlockState().getBlock() instanceof StorageBusBlock
                        && take(level, next, direction.getOpposite(), wanted)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean take(Level level, BlockPos bus, Direction enteredFromNetwork, ItemStack wanted) {
        for (Direction direction : Direction.values()) {
            if (direction == enteredFromNetwork) {
                continue;
            }
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, bus.relative(direction), direction.getOpposite());
            if (handler == null) {
                continue;
            }
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack simulated = handler.extractItem(slot, 1, true);
                if (!ItemStack.isSameItem(simulated, wanted)) {
                    continue;
                }
                ItemRoute route = PipeRouteFinder.find(level, bus, direction, simulated);
                if (route.isEmpty()) {
                    continue;
                }
                ItemPipeBlockEntity pipe = (ItemPipeBlockEntity) level.getBlockEntity(bus);
                if (pipe != null && pipe.insert(simulated, direction, route) == simulated.getCount()) {
                    handler.extractItem(slot, 1, false);
                    return true;
                }
            }
        }
        return false;
    }

    private static NetworkCrafterBlockEntity findCrafter(Level level, BlockPos origin, ItemStack wanted) {
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);
        seen.add(origin);
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (!seen.add(next)) {
                    continue;
                }
                if (level.getBlockEntity(next) instanceof NetworkCrafterBlockEntity crafter && crafter.result() == wanted.getItem()) {
                    return crafter;
                }
                if (level.getBlockEntity(next) instanceof ItemPipeBlockEntity) {
                    queue.add(next);
                }
            }
        }
        return null;
    }

    private static PipeDriverBlockEntity findDriver(Level level, BlockPos origin) {
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);
        seen.add(origin);
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (!seen.add(next)) {
                    continue;
                }
                if (level.getBlockEntity(next) instanceof PipeDriverBlockEntity driver) {
                    return driver;
                }
                if (level.getBlockEntity(next) instanceof ItemPipeBlockEntity) {
                    queue.add(next);
                }
            }
        }
        return null;
    }

    private static BlockPos adjacentPipe(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).getBlock() instanceof ItemPipeBlock) {
                return pos.relative(direction);
            }
        }
        return null;
    }

    private static Direction directionBetween(BlockPos from, BlockPos to) {
        return Direction.getNearest(to.getX() - from.getX(), to.getY() - from.getY(), to.getZ() - from.getZ());
    }
}
