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

import com.neep.meatlib.util.graph.PipeVertex;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.TransactionContext;
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

public interface ItemPipe {
    boolean isConnected(Level world, BlockPos pos, BlockState state, Direction direction);

    static boolean isConnectedIn(Level world, BlockPos pos, BlockState state, Direction direction) {
        return state.getBlock() instanceof ItemPipe pipe && pipe.isConnected(world, pos, state, direction);
    }

    default boolean isConnected(BlockGetter world, BlockPos pos, BlockState state, Direction direction) {
        return world instanceof Level level && isConnected(level, pos, state, direction);
    }

    default boolean canConnect(BlockGetter world, BlockPos pos, BlockState state, Direction direction) {
        return false;
    }

    default boolean canItemEnter(ItemVariant variant, long amount, Level world, BlockPos pos, BlockState state, Direction inFace, boolean routed) {
        return true;
    }

    default boolean canItemLeave(com.neep.meatlib.transfer.ResourceAmount<ItemVariant> item, Level world, BlockPos pos, BlockState state, Direction outFace) {
        return true;
    }

    default PipeVertex getPipeVertex(ServerLevel world, BlockPos pos, BlockState state) {
        return null;
    }

    default PipeConnections getConnections(BlockState state, @Nullable Direction forbidden) {
        return new PipeConnections();
    }

    default boolean endPoint() {
        return false;
    }

    default boolean supportsRouting() {
        return false;
    }

    default boolean alwaysAutoConnect(BlockState state, Direction face) {
        return false;
    }

    default boolean singleOutput() {
        return false;
    }

    default boolean prioritise() {
        return false;
    }

    default boolean canConnectTo(BlockState hereState, BlockState toState, Direction toFace, Level world, BlockPos toPos) {
        return false;
    }

    default VoxelShape getShapeForState(BlockState state) {
        return net.minecraft.world.phys.shapes.Shapes.block();
    }
}
