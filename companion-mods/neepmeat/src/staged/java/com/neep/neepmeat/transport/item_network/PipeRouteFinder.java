/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.transport.item_network;

import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.neepmeat.transport.util.ItemPipeUtil;
import com.neep.neepmeat.transport.util.PipePathfinding;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.ResourceAmount;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

/// A priority queue is used to prioritise filter pipes over other branches.
/// Route finding works, but does not adequately implement the desired round-robin behaviour at junctions.
/// Route finalisation is currently disabled as it is unused.
public class PipeRouteFinder
{
    private final ServerLevel world;
    private final BlockPos startPipe;

    public PipeRouteFinder(ServerLevel world, BlockPos startPipe)
    {
        this.world = world;
        this.startPipe = startPipe;
    }

    public void thing()
    {
        Operation op = start(new ResourceAmount<>(ItemVariant.of(Items.STONE), 1), s -> true, Direction.NORTH);

        op.iterate(null);


    }

//        public void clear()
//        {
//            visited.clear();
//            vertexToNode.clear();
//            frontier.clear();
//        }

    public Operation start(ResourceAmount<ItemVariant> item, Predicate<Storage<ItemVariant>> exclude, Direction inFace)
    {
        var startVertex = ItemPipeVertex.find(world, startPipe);
        return new Operation(item, startVertex, inFace, exclude);
    }

    public Operation run(ResourceAmount<ItemVariant> item, Predicate<Storage<ItemVariant>> exclude, Direction inFace, @Nullable TransactionContext transaction)
    {
        return start(item, exclude, inFace).run(transaction);
    }

    public static class Node implements Comparable<Node>
    {
        public final ItemPipeVertex vertex;
        public int cost;
        public Node parent;
        // Direction from the parent vertex to get here
        public Direction parentOutFace;
        // Direction that the item will enter from
        public Direction inFace;

        public Node(ItemPipeVertex vertex)
        {
            Objects.requireNonNull(vertex);
            this.vertex = vertex;
        }

        @Override
        public int compareTo(@NotNull Node other)
        {
            return Integer.compare(cost, other.cost);
        }
    }

    public class Operation
    {
        private final ResourceAmount<ItemVariant> item;
        private final Predicate<Storage<ItemVariant>> exclude;
        private final Direction inFace;
        private final ItemPipeVertex startVertex;

        //            private final int[] sortedIndices = new int[6];
        private final Map<ItemPipeVertex, Node> vertexToNode = new HashMap<>();
        private final Set<ItemPipeVertex> visited = new HashSet<>();
        private final PriorityQueue<Node> frontier = new ObjectHeapPriorityQueue<>();

        private long maxAmount = 0;
        private final List<ItemRoute> routes = new ObjectArrayList<>();

        private PipePathfinding.OperationState state = PipePathfinding.OperationState.UNFINISHED;

        public Operation(ResourceAmount<ItemVariant> item, ItemPipeVertex startVertex, Direction inFace, Predicate<Storage<ItemVariant>> exclude)
        {
            this.item = item;
            this.startVertex = startVertex;
            this.exclude = exclude;
            this.inFace = inFace;

            var startNode = vertexToNode.computeIfAbsent(startVertex, Node::new);
            startNode.inFace = inFace;
            frontier.enqueue(startNode);
            visited.add(startVertex);
        }

        public Operation run(TransactionContext transaction)
        {
            while (state == PipePathfinding.OperationState.UNFINISHED)
            {
                iterate(transaction);
            }

            return this;
        }

        public void iterate(TransactionContext transaction)
        {
            if (!frontier.isEmpty())
            {
                if (processVertex(item, startVertex, inFace, transaction))
                {
                    state = PipePathfinding.OperationState.SUCCESS;
                }
            }
            else
            {
                state = PipePathfinding.OperationState.FAIL;
            }
        }

        /// @return True if an exit was found
        private boolean processVertex(ResourceAmount<ItemVariant> item, ItemPipeVertex startVertex, Direction inFace, TransactionContext transaction)
        {
            var node = frontier.dequeue();

//                int output = node.vertex.getCurrentOutput(node.inFace);
//                miserableShift(output, sortedIndices, node.vertex.getSortedIndices());

            boolean foundRoute = false;

            for (int dir : node.vertex.getSortedIndices())
            {
                Direction face = Direction.values()[dir];

                if ((node.vertex == startVertex && face == inFace)
                        || !node.vertex.canItemLeave(item, face, true))
                    continue;

                // First, check adjacent vertices
                var adj = node.vertex.getAdjVertex(dir);
                if (adj != null)
                {
                    var child = vertexToNode.computeIfAbsent(adj, Node::new);

                    if (!visited.contains(adj))
                    {
                        visited.add(adj);

                        Direction enterDirection = adj.getDirection(node.vertex);
                        if (adj.canItemEnter(item.resource(), item.amount(), enterDirection, false))
                        {
                            child.cost = node.cost;
//                                if (dir == output && child.vertex.prioritise())
                            if (child.vertex.prioritise())
                                child.cost -= 1;

                            child.parent = node;
                            child.parentOutFace = face;
//                            child.inFace = inFace;
                            frontier.enqueue(child);
                        }
                    }
                }
                else // Don't try storage if there is a pipe
                {
                    var storage = node.vertex.getStorage(face);
                    if (exclude.test(storage) && storage != null)
                    {
                        long transferable = MeatlibStorageUtil.simulateInsert(storage, item.resource(), Long.MAX_VALUE, transaction);
                        if (transferable > 0)
                        {
                            maxAmount = transferable;

                            // TODO: Implement round-robin and use the route
//                            routes.add(PipePathfinding.makeRoute(world, node, face));
                            foundRoute = true;
                        }
                    }
                    else // Only try to dump if everything else fails
                    {
                        BlockPos nodePos = node.vertex.getPos();
                        BlockPos offsetPos = nodePos.relative(face);
                        BlockState offsetState = world.getBlockState(offsetPos);
                        if (node.vertex.isConnected(face)
                                && ItemPipeUtil.canDumpInto(world, offsetPos, offsetState))
                        {
                            maxAmount = Integer.MAX_VALUE;

                            // TODO: Implement round-robin and use the route
//                            routes.add(PipePathfinding.makeRoute(world, node, face));
                            foundRoute = true;
                        }
                    }
                }
            }

            return foundRoute;
        }

        public List<ItemRoute> routes()
        {
            return routes;
        }

        public ItemRoute firstRoute()
        {
            return routes.isEmpty() ? ItemRoute.EMPTY : routes.get(0);
        }

        public long maxAmount()
        {
            return maxAmount;
        }
    }
}
