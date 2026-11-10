/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Priority-queue route search from upstream PipeRouteFinder. A filter pipe
 * (prioritise) costs one less than a plain pipe, so it is expanded first.
 * The first exit whose handler accepts the stack under simulate wins.
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public final class PipeRouteFinder {
    private PipeRouteFinder() {}

    public static ItemRoute find(Level level, BlockPos start, Direction enteredFrom, ItemStack stack) {
        PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator.comparingInt(node -> node.cost));
        Set<BlockPos> visited = new HashSet<>();
        Node startNode = new Node(start, 0, null, null);
        frontier.add(startNode);
        visited.add(start);
        while (!frontier.isEmpty()) {
            Node node = frontier.poll();
            for (Direction direction : Direction.values()) {
                if (node == startNode && direction == enteredFrom) {
                    continue;
                }
                BlockPos next = node.pos.relative(direction);
                BlockState state = level.getBlockState(next);
                if (state.getBlock() instanceof ItemPipeBlock) {
                    if (!(level.getBlockEntity(next) instanceof ItemPipeBlockEntity pipe) || !pipe.allows(stack) || !visited.add(next)) {
                        continue;
                    }
                    int cost = node.cost + (pipe.prioritise() ? -1 : 1);
                    frontier.add(new Node(next, cost, node, direction));
                    continue;
                }
                IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, next, direction.getOpposite());
                if (handler != null && ItemHandlerHelper.insertItem(handler, stack.copy(), true).isEmpty()) {
                    return route(node, direction);
                }
            }
        }
        return ItemRoute.EMPTY;
    }

    private static ItemRoute route(Node node, Direction last) {
        List<Direction> steps = new ArrayList<>();
        steps.add(last);
        while (node.exit != null) {
            steps.add(node.exit);
            node = node.parent;
        }
        java.util.Collections.reverse(steps);
        return new ItemRoute(steps);
    }

    private static final class Node {
        private final BlockPos pos;
        private final int cost;
        private final Node parent;
        private final Direction exit;

        private Node(BlockPos pos, int cost, Node parent, Direction exit) {
            this.pos = pos;
            this.cost = cost;
            this.parent = parent;
            this.exit = exit;
        }
    }
}
