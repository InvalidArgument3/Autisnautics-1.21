/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port of PipeGraphUtil. Straight runs (two connections, simplifiable)
 * are hidden. Junctions and pipes that refuse to simplify become vertices and
 * point at each other across the hidden run.
 */

package com.neep.neepmeat.transport;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.neep.meatlib.util.graph.ConnectablePipe;
import com.neep.meatlib.util.graph.PipeVertex;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public final class PipeGraph {
    private PipeGraph() {}

    public static <T extends PipeVertex> void linkVertices(ServerLevel level, BlockPos pos, BlockState state, ConnectablePipe pipe, PipeVertex.Lookup<T> lookup, ConnectablePipe.Find find) {
        List<Pair<T, Direction>> toConnect = Lists.newArrayList();
        for (Direction direction : pipe.getConnections(state)) {
            Pair<T, Direction> next = findNextVertex(level, pos, direction, lookup, find);
            if (next != null) {
                toConnect.add(next);
            }
        }
        if (toConnect.size() != 2) {
            return;
        }
        Pair<T, Direction> first = toConnect.get(0);
        Pair<T, Direction> second = toConnect.get(1);
        first.first().setAdjVertex(first.second().get3DDataValue(), second.first());
        second.first().setAdjVertex(second.second().get3DDataValue(), first.first());
    }

    public static <T extends PipeVertex> Pair<T, Direction> findNextVertex(ServerLevel level, BlockPos pos, Direction out, PipeVertex.Lookup<T> lookup, ConnectablePipe.Find find) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<Pair<BlockPos, Direction>> queue = Queues.newArrayDeque();
        queue.add(Pair.of(pos.relative(out), out));
        visited.add(pos.relative(out));
        visited.add(pos);
        while (!queue.isEmpty()) {
            Pair<BlockPos, Direction> current = queue.poll();
            BlockState currentState = level.getBlockState(current.first());
            ConnectablePipe pipe = find.find(level, current.first(), currentState);
            if (pipe == null) {
                return null;
            }
            T vertex = lookup.find(level, current.first());
            if (vertex == null) {
                throw new IllegalStateException("Pipe " + pipe + " has no vertex");
            }
            if (pipe.countConnections(currentState) > 2 || !vertex.canSimplify()) {
                return Pair.of(vertex, current.second().getOpposite());
            }
            for (Direction direction : pipe.getConnections(currentState)) {
                BlockPos next = current.first().relative(direction);
                if (!visited.contains(next) && lookup.find(level, next) != null) {
                    visited.add(next);
                    queue.add(Pair.of(next, direction));
                }
            }
        }
        return null;
    }

    public static void eraseHiddenFromNeighbours(ServerLevel level, BlockPos pos, BlockState state, ConnectablePipe pipe, ConnectablePipe.Find find) {
        for (Pair<PipeVertex, Direction> pair : nearestVertices(level, pos, state, pipe, find)) {
            pair.first().setAdjVertex(pair.second().getOpposite().get3DDataValue(), null);
        }
    }

    public static List<Pair<PipeVertex, Direction>> nearestVertices(ServerLevel level, BlockPos start, BlockState oldState, ConnectablePipe startPipe, ConnectablePipe.Find find) {
        List<Pair<PipeVertex, Direction>> vertices = Lists.newArrayList();
        PipeVertex startVertex = startPipe.getPipeVertex(level, start, oldState);
        if (startVertex == null || !startVertex.canSimplify()) {
            return vertices;
        }
        Set<BlockPos> visited = Sets.newHashSet();
        ArrayDeque<BlockPos> stack = new ArrayDeque<>();
        ArrayDeque<BlockState> states = new ArrayDeque<>();
        ArrayDeque<ConnectablePipe> pipes = new ArrayDeque<>();
        visited.add(start);
        stack.add(start);
        pipes.add(startPipe);
        states.add(oldState);
        while (!stack.isEmpty()) {
            BlockPos current = stack.poll();
            ConnectablePipe currentPipe = pipes.poll();
            BlockState currentState = states.poll();
            for (Direction direction : currentPipe.getConnections(currentState)) {
                BlockPos next = current.relative(direction);
                if (visited.contains(next)) {
                    continue;
                }
                BlockState offsetState = level.getBlockState(next);
                ConnectablePipe offsetPipe = find.find(level, next, offsetState);
                if (offsetPipe == null) {
                    continue;
                }
                PipeVertex vertex = offsetPipe.getPipeVertex(level, next, offsetState);
                if (vertex != null && !vertex.canSimplify()) {
                    vertices.add(Pair.of(vertex, direction));
                    continue;
                }
                visited.add(next);
                stack.add(next);
                states.add(offsetState);
                pipes.add(offsetPipe);
            }
        }
        return vertices;
    }
}
