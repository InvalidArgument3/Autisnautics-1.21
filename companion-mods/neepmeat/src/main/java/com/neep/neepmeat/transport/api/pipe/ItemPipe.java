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
 *
 * Connection queries. The routed item itself stays with the staged pipe network.
 */
package com.neep.neepmeat.transport.api.pipe;

import com.neep.meatlib.util.graph.ConnectablePipe;
import com.neep.meatlib.util.graph.PipeVertex;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.TransactionContext;
import com.neep.neepmeat.transport.item_network.ItemInPipe;
import com.neep.neepmeat.transport.item_network.ItemPipeVertex;
import com.neep.meatlib.util.PipeConnections;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public interface ItemPipe extends ConnectablePipe {
    boolean isConnected(Level world, BlockPos pos, BlockState state, Direction direction);

    /** Upstream leaves insertion abstract. The item and its removal callback live on {@link ItemInPipe} and {@code PipeItemMemory}. */
    long insert(Level world, BlockPos pos, BlockState state, Direction direction, ItemInPipe item, TransactionContext transaction);

    static boolean isConnectedIn(Level world, BlockPos pos, BlockState state, Direction direction) {
        return state.getBlock() instanceof ItemPipe pipe && pipe.isConnected(world, pos, state, direction);
    }

    default boolean isConnected(BlockGetter world, BlockPos pos, BlockState state, Direction direction) {
        return world instanceof Level level && isConnected(level, pos, state, direction);
    }

    /** Upstream connects unless a pipe overrides this. */
    default boolean canConnect(BlockGetter world, BlockPos pos, BlockState state, Direction direction) {
        return true;
    }

    /** Upstream admits an item only through a connected face. */
    default boolean canItemEnter(ItemVariant variant, long amount, Level world, BlockPos pos, BlockState state, Direction inFace, boolean routed) {
        return isConnectedIn(world, pos, state, inFace);
    }

    /** Upstream lets an item leave only through a connected face. */
    default boolean canItemLeave(com.neep.meatlib.transfer.ResourceAmount<ItemVariant> item, Level world, BlockPos pos, BlockState state, Direction outFace) {
        return isConnectedIn(world, pos, state, outFace);
    }

    @Override
    default PipeVertex getPipeVertex(ServerLevel world, BlockPos pos, BlockState state) {
        return ItemPipeVertex.NULL;
    }

    @Override
    default PipeConnections getConnections(BlockState state) {
        return getConnections(state, null);
    }

    /** Upstream walks connection properties on a precise pipe and returns an empty set otherwise. */
    default PipeConnections getConnections(BlockState state, @Nullable Direction forbidden) {
        if (!(state.getBlock() instanceof PrecisePipeBlock)) {
            return new PipeConnections();
        }
        PipeConnections set = new PipeConnections();
        for (var entry : PrecisePipeBlock.DIR_TO_CONNECTION.entrySet()) {
            if (state.getValue(entry.getValue()).isConnected() && entry.getKey() != forbidden) {
                set.set(entry.getKey());
            }
        }
        return set;
    }

    /** Upstream treats a plain pipe end as a routing vertex. */
    default boolean endPoint() {
        return true;
    }

    default boolean supportsRouting() {
        return false;
    }

    default boolean alwaysAutoConnect(BlockState state, Direction face) {
        return false;
    }

    /** Upstream pipes have one output unless a router overrides this. */
    default boolean singleOutput() {
        return true;
    }

    default boolean prioritise() {
        return false;
    }

    default boolean canConnectTo(BlockState hereState, BlockState toState, Direction toFace, Level world, BlockPos toPos) {
        return false;
    }

    default VoxelShape getShapeForState(BlockState state) {
        if (state.getBlock() instanceof PrecisePipeBlock pipe) {
            return pipe.getShapeForState(state);
        }
        return net.minecraft.world.phys.shapes.Shapes.block();
    }
}
