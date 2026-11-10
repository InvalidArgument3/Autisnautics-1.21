/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Breadth-first route from a pipe to the first inventory that can take the stack.
 * Filter pipes that reject the stack are not entered. Simulate is used before the
 * route is accepted.
 */

package com.neep.neepmeat.machine.transport;

import com.neep.neepmeat.transport.ItemRoute;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PipeRouteFinder {
    private PipeRouteFinder() {}

    public static ItemRoute find(Level level, BlockPos start, Direction enteredFrom, ItemStack stack) {
        record Step(BlockPos pos, List<Direction> route) {}
        ArrayDeque<Step> queue = new ArrayDeque<>();
        Set<BlockPos> seen = new HashSet<>();
        queue.add(new Step(start, List.of()));
        seen.add(start);
        while (!queue.isEmpty()) {
            Step step = queue.poll();
            if (!(level.getBlockEntity(step.pos) instanceof ItemPipeBlockEntity pipe) || !pipe.allows(stack)) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                if (direction == enteredFrom && step.pos.equals(start)) {
                    continue;
                }
                BlockPos next = step.pos.relative(direction);
                BlockState state = level.getBlockState(next);
                List<Direction> route = new java.util.ArrayList<>(step.route);
                route.add(direction);
                if (state.getBlock() instanceof ItemPipeBlock && seen.add(next)) {
                    queue.add(new Step(next, route));
                    continue;
                }
                IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, next, direction.getOpposite());
                if (handler != null && ItemHandlerHelper.insertItem(handler, stack.copy(), true).isEmpty()) {
                    return new ItemRoute(route);
                }
            }
            enteredFrom = null;
        }
        return ItemRoute.EMPTY;
    }
}
