/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Priority-queue route search from upstream PipeRouteFinder. A filter pipe
 * (prioritise) costs one less than a plain pipe, so it is expanded first.
 * The first exit whose handler accepts the stack under simulate wins.
 */

package com.neep.neepmeat.transport.item_network;
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
import com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity;

import com.neep.neepmeat.transport.ItemRoute;
import com.neep.neepmeat.transport.item_network.ItemPipeVertex;
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
        PriorityQueue<Search> frontier = new PriorityQueue<>(Comparator.comparingInt(node -> node.cost));
        Set<BlockPos> visited = new HashSet<>();
        Search startNode = new Search(start, 0, null, null);
        frontier.add(startNode);
        visited.add(start);
        while (!frontier.isEmpty()) {
            Search node = frontier.poll();
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
                    frontier.add(new Search(next, cost, node, direction));
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

    private static ItemRoute route(Search node, Direction last) {
        List<Direction> steps = new ArrayList<>();
        steps.add(last);
        while (node.exit != null) {
            steps.add(node.exit);
            node = node.parent;
        }
        java.util.Collections.reverse(steps);
        return new ItemRoute(steps);
    }

    private static final class Search {
        private final BlockPos pos;
        private final int cost;
        private final Search parent;
        private final Direction exit;

        private Search(BlockPos pos, int cost, Search parent, Direction exit) {
            this.pos = pos;
            this.cost = cost;
            this.parent = parent;
            this.exit = exit;
        }
    }

    /** Vertex node used by the item-network pathfinder. */
    public static final class Node implements Comparable<Node> {
        public final ItemPipeVertex vertex;
        public int cost;
        public Node parent;
        public Direction parentOutFace;
        public Direction inFace;

        public Node(ItemPipeVertex vertex) {
            this.vertex = java.util.Objects.requireNonNull(vertex);
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(cost, other.cost);
        }
    }
}
