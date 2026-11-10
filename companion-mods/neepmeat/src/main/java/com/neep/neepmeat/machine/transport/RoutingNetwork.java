/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Request path from RoutingNetworkImpl: find a storage bus that can supply the
 * stack, simulate the extract, build a priority route, then extract and insert.
 * Crafting requests and the deferred RoutablePipe callbacks are not included.
 */

package com.neep.neepmeat.machine.transport;

import com.neep.neepmeat.transport.ItemRoute;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public final class RoutingNetwork {
    private RoutingNetwork() {}

    public static boolean request(Level level, BlockPos destination, ItemStack wanted) {
        if (wanted.isEmpty()) {
            return false;
        }
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
                if (!(pipe.getBlockState().getBlock() instanceof StorageBusBlock)) {
                    continue;
                }
                if (supply(level, next, direction.getOpposite(), wanted)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean supply(Level level, BlockPos bus, Direction enteredFromNetwork, ItemStack wanted) {
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
}
