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

package com.neep.neepmeat.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.neep.meatlib.util.graph.ConnectablePipe;
import com.neep.meatlib.util.graph.PipeVertex;
import com.neep.neepmeat.transport.item_network.ItemPipeVertex;
import it.unimi.dsi.fastutil.Pair;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class PipeGraphUtil
{
    // For use if this pipe is new and may connect two vertices.
    public static <T extends PipeVertex> void linkVertices(ServerLevel world, BlockPos pos, BlockState state, ConnectablePipe pipe, PipeVertex.Lookup<T> lookup, ConnectablePipe.Find find)
    {
        List<Pair<T, Direction>> toConnect = Lists.newArrayList();
        for (Direction direction : pipe.getConnections(state))
        {
            var nextVertex = findNextVertex(world, pos, direction, lookup, find);
            if (nextVertex != null)
            {
                toConnect.add(nextVertex);
            }
        }

        // Pipe is non-trivial or an end
        if (toConnect.size() != 2)
            return;

        var vertex1 = toConnect.get(0);
        var vertex2 = toConnect.get(1);

        vertex1.first().setAdjVertex(vertex1.second().get3DDataValue(), vertex2.first());
        vertex2.first().setAdjVertex(vertex2.second().get3DDataValue(), vertex1.first());

        jankParticles(world, vertex1.first(), true);
        jankParticles(world, vertex2.first(), true);
    }

    public static void jankParticles(ServerLevel world, PipeVertex vertex, boolean add)
    {
//        jankParticles1(world, vertex, add);
    }

    public static void jankParticles1(ServerLevel world, PipeVertex vertex, boolean add)
    {
        if (!add)
            return;

        BlockPos particlePos = BlockPos.ZERO;
        if (vertex instanceof ItemPipeVertex ipv)
        {
            particlePos = ipv.getPos();
        }

        var type = add ? ParticleTypes.COMPOSTER : ParticleTypes.POOF;

        world.sendParticles(
                type,
                particlePos.getX() + 0.5, particlePos.getY() + 0.5, particlePos.getZ() + 0.5,
                10, 0.25, 0.25, 0.25, 0.01);
    }

    public static void moreJankParticles(ServerLevel world, PipeVertex vertex)
    {
//        BlockPos particlePos = BlockPos.ORIGIN;
//        if (vertex instanceof BlockFluidPipeVertex bpv)
//        {
//            particlePos = BlockPos.fromLong(bpv.getPos());
//        }
//        if (vertex instanceof ItemPipeVertex ipv)
//        {
//            particlePos = ipv.getPos();
//        }
//
//        world.spawnParticles(
//                ParticleTypes.END_ROD,
//                particlePos.getX() + 0.5, particlePos.getY() + 0.5, particlePos.getZ() + 0.5,
//                2, 0.1, 0.1, 0.1, 0.01);
    }

    public static <T extends PipeVertex> Pair<T, Direction> findNextVertex(ServerLevel world, BlockPos pos, Direction out, PipeVertex.Lookup<T> lookup, ConnectablePipe.Find find)
    {
        Set<BlockPos> visited = new HashSet<>(); // TODO: convert to long set
        Queue<Pair<BlockPos, Direction>> queue = Queues.newArrayDeque();

        queue.add(Pair.of(pos.relative(out), out));
        visited.add(pos.relative(out));
        visited.add(pos);

        while (!queue.isEmpty())
        {
            var current = queue.poll();
            BlockState currentState = world.getBlockState(current.first());
            var pipe = find.find(world, current.first(), currentState);
//            FluidPipe pipe = FluidPipe.findFluidPipe(world, current.first(), currentState);

            // This indicates that the connection is to a node, so no further action is required.
            if (pipe == null)
                return null;

            int connections = pipe.countConnections(currentState);

            T vertex = lookup.find(world, current.first());

            if (vertex == null)
                throw new IllegalStateException("Pipe %s does not have valid vertex lookup. Fix this.".formatted(pipe));

            if (connections > 2 || !vertex.canSimplify())
            {
//                jankParticles(vertex);
                return Pair.of(vertex, current.second().getOpposite());
            }

            BlockPos.MutableBlockPos offsetPos = current.first().mutable();
            for (Direction direction : pipe.getConnections(currentState))
            {
                offsetPos.setWithOffset(current.first(), direction);

                if (!visited.contains(offsetPos))
                {
                    T offsetVertex = lookup.find(world, offsetPos);

                    if (offsetVertex == null)
                        continue;

                    visited.add(offsetPos.immutable());
                    queue.add(Pair.of(offsetPos.immutable(), direction));
                }
            }
        }

        return null;
    }

    /// For intermediate pipes. Finds the nearest non-trivial neighbours and disconnects them from each other.
    public static void eraseHiddenFromNeighbours(ServerLevel world, BlockPos pos, BlockState state, ConnectablePipe pipe, ConnectablePipe.Find find)
    {
        var toNotify = PipeGraphUtil.getNearestVertices(world, pos, state, pipe, find);
        for (var pair : toNotify)
        {
            pair.first().setAdjVertex(pair.second().getOpposite().ordinal(), null);
            jankParticles(world, pair.first(), false);
        }
    }

    /// For intermediate pipes with 2 connections
    public static List<Pair<PipeVertex, Direction>> getNearestVertices(ServerLevel world, BlockPos start, BlockState oldState, ConnectablePipe startPipe, ConnectablePipe.Find find)
    {
        List<Pair<PipeVertex, Direction>> vertices = Lists.newArrayList();

        Set<BlockPos> visited = Sets.newHashSet();
        Deque<BlockPos> stack = Queues.newArrayDeque();
        Deque<BlockState> stateQueue = Queues.newArrayDeque();
        Deque<ConnectablePipe> pipeQueue = Queues.newArrayDeque();

        var startVertex = startPipe.getPipeVertex(world, start, oldState);

        // This method only applies to intermediate
        if (!startVertex.canSimplify())
        {
            return vertices;
        }

        visited.add(start);
        stack.add(start);
        pipeQueue.add(startPipe);
        stateQueue.add(oldState);

        while (!stack.isEmpty())
        {
            BlockPos current = stack.poll();
            var currentPipe = pipeQueue.poll();
            BlockState currentState = stateQueue.poll();

            BlockPos.MutableBlockPos mutable = current.mutable();

            for (Direction direction : currentPipe.getConnections(currentState))
            {
                mutable.setWithOffset(current, direction);

                if (visited.contains(mutable))
                    continue;

                BlockState offsetState = world.getBlockState(mutable);
                ConnectablePipe offsetPipe = find.find(world, mutable, offsetState);
                if (offsetPipe != null)
                {
                    var vertex = offsetPipe.getPipeVertex(world, mutable, offsetState);
                    if (vertex != null && !vertex.canSimplify())
                    {
                        vertices.add(Pair.of(vertex, direction));
                        continue;
                    }

                    visited.add(mutable.immutable());
                    stack.add(mutable.immutable());
                    stateQueue.add(offsetState);
                    pipeQueue.add(offsetPipe);
                }
            }
        }
        return vertices;
    }
}
