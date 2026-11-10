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

package com.neep.neepmeat.transport.util;

import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.neepmeat.util.PipeGraphUtil;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.item_network.ItemPipeVertex;
import com.neep.neepmeat.transport.item_network.ItemRoute;
import com.neep.neepmeat.transport.item_network.PipeRouteFinder;
import it.unimi.dsi.fastutil.longs.LongObjectPair;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.ResourceAmount;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PipePathfinding
{
    // Puts preferred at the beginning of sorted and copies the contents of input without preferred
    private static void miserableShift(int preferred, int[] sorted, int[] input)
    {
        if (preferred == -1)
        {
            System.arraycopy(input, 0, sorted, 0, input.length);
            return;
        }

        sorted[0] = preferred;

        int i = 1;
        int j = 0;
        while (i < input.length)
        {
            if (input[j] == preferred)
                j++;

            sorted[i] = input[j];

            j++;
            i++;
        }
    }

    public enum OperationState
    {
        UNFINISHED,
        SUCCESS,
        FAIL
    }

    public static ItemRoute makeRoute(ServerLevel world, PipeRouteFinder.Node node, Direction face)
    {
        ItemRoute route = new ItemRoute();
        PipeRouteFinder.Node n = node;
        while (n != null)
        {
            PipeGraphUtil.moreJankParticles(world, n.vertex);

            route.add(n.parentOutFace);
//            n.vertex.nextOutput(n.inFace);
            n = n.parent;
        }

        route.remove(route.size() - 1);
        Collections.reverse(route);
        route.add(face);

        // TODO: remove
//        System.out.println(route);
        return route;
    }

    // Not particularly useful when all edge weights are 1
    public static LongObjectPair<ItemRoute> uniformCostCapacity(ResourceAmount<ItemVariant> item, ServerLevel world, BlockPos startPipe, Direction exit, @Nullable TransactionContext transaction)
    {
        var startVertex = ItemPipeVertex.find(world, startPipe);

//        ObjectHeapPriorityQueue<ItemPipeVertex> frontier = new ObjectHeapPriorityQueue<>();
        java.util.PriorityQueue<PipeRouteFinder.Node> frontier = new java.util.PriorityQueue<>();
        Set<ItemPipeVertex> visited = new HashSet<>();
        Map<ItemPipeVertex, PipeRouteFinder.Node> vertexToNode = new HashMap<>();

        var startNode = vertexToNode.computeIfAbsent(startVertex, PipeRouteFinder.Node::new);
        frontier.add(startNode);
        visited.add(startVertex);

        while (!frontier.isEmpty())
        {
            var node = frontier.poll();

            for (Direction face : Direction.values())
            {
                // TODO: exit check
                var storage = node.vertex.getStorage(face);
                if (storage != null)
                {
                    long transferable = MeatlibStorageUtil.simulateInsert(storage, item.resource(), Long.MAX_VALUE, transaction);
                    if (transferable > 0)
                    {
                        return LongObjectPair.of(transferable, makeRoute(world, node, face));
                    }
                }

                var adj = node.vertex.getAdjVertex(face.ordinal());
                if (adj != null)
                {
                    var child = vertexToNode.computeIfAbsent(adj, PipeRouteFinder.Node::new);
                    child.cost = node.cost + 1; // Edge weights are always 1 because I can't be bothered

                    // TODO: insertion check
                    if (!visited.contains(adj))
                    {
                        child.parent = node;
                        child.parentOutFace = face;
                        visited.add(adj);
                        frontier.add(child);
                    }
                    else if (child.cost > node.cost && frontier.contains(child))
                    {
                        child.parent = node;
                        child.parentOutFace = face;
                        frontier.remove(child);
                        frontier.add(child);
                    }
                }
            }
        }

        return LongObjectPair.of(0, ItemRoute.EMPTY);
    }

    // Uses a DFS to return a path to the found storage.
    // Chooses the first storage encountered, not the closest.
    public static LongObjectPair<ItemRoute> singleRouteCapacityDFS(ResourceAmount<ItemVariant> item, ServerLevel world, BlockPos startPipe, Direction exit, @Nullable TransactionContext transaction)
    {
        var startVertex = ItemPipeVertex.find(world, startPipe);
        if (startVertex == null)
            return LongObjectPair.of(0, ItemRoute.EMPTY);

        Set<ItemPipeVertex> visited = new HashSet<>();
        ObjectArrayList<ItemPipeVertex> vertexRoute = new ObjectArrayList<>();
        ObjectArrayList<Direction> route = new ObjectArrayList<>();

        visited.add(startVertex);

        ItemPipeVertex vertex = startVertex;
        Direction currentDirection = exit;

        whileBlock: while (true)
        {
            for (Direction direction : Direction.values())
            {
                // Check storage first
                var storage = vertex.getStorage(direction);
                if (storage != null)
                {
                    long transferable = MeatlibStorageUtil.simulateInsert(storage, item.resource(), Long.MAX_VALUE, transaction);
                    if (transferable > 0)
                    {
                        // There's probably an elegant way of avoiding this
                        route.remove(0);
                        route.push(direction);
                        return LongObjectPair.of(transferable, new ItemRoute(route));
                    }
                }

                // Look for adjacent vertices
                var adj = vertex.getAdjVertex(direction.ordinal());
                if (adj != null && !visited.contains(adj))
                {
                    visited.add(adj);

                    Direction enterDirection = adj.getDirection(vertex);
                    if (adj.canItemEnter(item.resource(), item.amount(), enterDirection, false))
                    {
                        route.push(currentDirection);
                        vertexRoute.push(vertex);

                        currentDirection = direction;
                        vertex = adj;

                        continue whileBlock;
                    }
                }
            }

            // If the route is empty after this you're on your own.
            if (route.isEmpty())
                return LongObjectPair.of(0, ItemRoute.EMPTY);

            // Step back
            vertex = vertexRoute.pop();
            currentDirection = route.pop();
        }

//        return LongObjectPair.of(0, ItemRoute.EMPTY);
    }

    // Chooses the storage with the least hops.
    // Doesn't return a path.
    public static LongObjectPair<ItemRoute> singleRouteCapacity2(ResourceAmount<ItemVariant> item, ServerLevel world, BlockPos startPipe, Direction exit, @Nullable TransactionContext transaction)
    {
        var startVertex = ItemPipeVertex.find(world, startPipe);

        Queue<ItemPipeVertex> queue = new ArrayDeque<>();
        Set<ItemPipeVertex> visited = new HashSet<>();
        queue.add(startVertex);
        visited.add(startVertex);

        while (!queue.isEmpty())
        {
            var vertex = queue.poll();
            if (vertex != null)
            {
                for (Direction direction : Direction.values())
                {
                    var adj = vertex.getAdjVertex(direction.ordinal());
                    if (adj != null && !visited.contains(adj))
                    {
                        visited.add(adj);

                        Direction enterDirection = adj.getDirection(vertex);

                        if (adj.canItemEnter(item.resource(), item.amount(), enterDirection, false))
                        {
                            queue.add(adj);
                        }
                        else
                        {
                            // TODO: storages as adjacent
                        }
                    }

                    var storage = vertex.getStorage(direction);
                    if (storage != null)
                    {
                        long transferable = MeatlibStorageUtil.simulateInsert(storage, item.resource(), Long.MAX_VALUE, transaction);
                        if (transferable > 0)
                        {
                            return LongObjectPair.of(transferable, ItemRoute.EMPTY);
                        }
                    }
                }
            }
        }

        return LongObjectPair.of(0, ItemRoute.EMPTY);
    }

    /** Simple route detection that does not take branches into account.
     * @return Number of items that can be safely transferred. Takes max value if a branch is met.
     */
    public static long singleRouteCapacity(ResourceAmount<ItemVariant> item, Level world, BlockPos startPipe, Direction exit, @Nullable TransactionContext transaction)
    {
        Queue<BlockPos> queue = new ArrayDeque<>();
        Queue<ItemPipe> pipeQueue = new ArrayDeque<>();
        Queue<Direction> dirQueue = new ArrayDeque<>();

        LongSet visited = new LongOpenHashSet();
        queue.add(startPipe);
        pipeQueue.add((ItemPipe) world.getBlockState(startPipe).getBlock());
        dirQueue.add(exit.getOpposite());
        visited.add(startPipe.relative(exit.getOpposite()).asLong());

        long alreadyInTransit = 0;

        while (!queue.isEmpty())
        {
            BlockPos current = queue.poll();
            ItemPipe currentPipe = pipeQueue.poll();
            Direction currentDir = dirQueue.poll();
            BlockState currentState = world.getBlockState(current);

            visited.add(current.asLong());

            var connections = currentPipe.getConnections(currentState, currentDir);

            if (!currentPipe.singleOutput() || connections.size() > 1)
                return item.amount();

            if (connections.isEmpty())
                return 0;

            Direction direction = connections.directions().iterator().next();

            BlockPos offset = current.relative(direction);
            BlockState offsetState = world.getBlockState(offset);

            if (world.getBlockEntity(current) instanceof ItemPipeBlockEntity be)
            {
                for (var i : be.getItems())
                {
                    if (i.resource().equals(item.resource()))
                        alreadyInTransit += i.amount();
                }
            }

            if (currentPipe.canItemLeave(item, world, current, currentState, direction, false))
            {
                if (ItemPipeUtil.canDumpInto(world, offset, offsetState))
                    return item.amount();

                Storage<ItemVariant> storage;
                if (offsetState.getBlock() instanceof ItemPipe pipe
                                && pipe.canItemEnter(item.resource(), item.amount() , world, offset, offsetState, direction.getOpposite(), false)
                                && !visited.contains(offset.asLong()))
                {
                    queue.add(offset);
                    pipeQueue.add(pipe);
                    dirQueue.add(direction.getOpposite());
                }
                else if ((storage = ItemStorage.SIDED.find(world, offset, offsetState, null, direction.getOpposite())) != null)
                {
                    return Math.max(0, MeatlibStorageUtil.simulateInsert(storage, item.resource(), Long.MAX_VALUE, transaction)
                            - alreadyInTransit);
                }
            }
        }
        return 0;
    }
}
