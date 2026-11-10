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

package com.neep.neepmeat.transport.api.pipe;

import com.neep.meatlib.util.PipeConnections;
import com.neep.meatlib.util.PipeConnectionsC;
import com.neep.meatlib.util.graph.ConnectablePipe;
import com.neep.meatlib.util.graph.PipeVertex;
import com.neep.neepmeat.transport.item_network.ItemInPipe;
import com.neep.neepmeat.transport.item_network.ItemPipeVertex;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface ItemPipe extends ConnectablePipe
{
    static boolean isConnectedIn(Level world, BlockPos pos, BlockState state, Direction direction)
    {
        if (state.getBlock() instanceof ItemPipe acceptor)
        {
            return acceptor.isConnected(world, pos, state, direction);
        }
        return false;
    }

    static void onStateReplaced(Level world, BlockPos pos, BlockState state, BlockState newState)
    {
        if (!world.isClientSide() && !newState.is(state.getBlock()))
        {
            if (state.getBlock() instanceof ItemPipe itemPipe && itemPipe.getPipeVertex((ServerLevel) world, pos, state) instanceof ItemPipeVertex vertex)
            {
                vertex.prepareForRemoval(state, newState);
            }
        }
    }

    @Nullable
    static ItemPipe findItemPipe(BlockState state)
    {
        if (state.getBlock() instanceof ItemPipe pipe)
            return pipe;
        return null;
    }

    static ConnectablePipe findItemPipe(Level world, BlockPos pos, BlockState state)
    {
        return findItemPipe(state);
    }

    @Override
    default PipeVertex getPipeVertex(ServerLevel world, BlockPos pos, BlockState state)
    {
        return ItemPipeVertex.NULL;
    }

    @Override
    default PipeConnections getConnections(BlockState state)
    {
        return getConnections(state, null);
    }

    default PipeConnections getConnections(BlockState state, @Nullable Direction forbidden)
    {
        if (!(state.getBlock() instanceof PrecisePipeBlock))
            return PipeConnections.EMPTY.copy();

        var set = new PipeConnections();
        for (var entry : PrecisePipeBlock.DIR_TO_CONNECTION.entrySet())
        {
            if (state.getValue(entry.getValue()).isConnected() && entry.getKey() != forbidden)
            {
                set.set(entry.getKey());
            }
        }
        return set;
    }

    default boolean alwaysAutoConnect(BlockState state, Direction face)
    {
        return false;
    }

    long insert(Level world, BlockPos pos, BlockState state, Direction direction, ItemInPipe item, TransactionContext transaction);

    default boolean canConnect(BlockGetter world, BlockPos pos, BlockState state, Direction face)
    {
        return true;
    }

    boolean isConnected(BlockGetter blockView, BlockPos pos, BlockState state, Direction face);

    default boolean canItemEnter(ItemVariant variant, long amount, Level world, BlockPos pos, BlockState state, Direction inFace, boolean routed)
    {
        return isConnectedIn(world, pos, state, inFace);
    }

    default boolean canItemLeave(ResourceAmount<ItemVariant> item, Level world, BlockPos pos, BlockState state, Direction outFace, boolean routed)
    {
        return isConnectedIn(world, pos, state, outFace);
    }

    static boolean all(Direction direction)
    {
        return true;
    }

    default Direction getOutputDirection(ItemInPipe item, BlockPos pos, BlockState state, Level world, Direction in, @Nullable BlockEntity be, boolean overridePathNode, TransactionContext transaction)
    {
        var connections = ((ItemPipe) state.getBlock()).getConnections(state, in);

        Direction out = item.getPreferredOutputDirection(state, in, this, overridePathNode);
        if (out != null && connections.contains(out))
            return out;

        var rand = world.getRandom();
        if (!connections.isEmpty())
        {
            out = connections.get(rand.nextInt(connections.size()));
        }
        else
        {
            out = in;
        }
        return out;
    }

    default boolean singleOutput()
    {
        return true;
    }

    default boolean supportsRouting()
    {
        return false;
    }

    default boolean prioritise()
    {
        return false;
    }

    /// Return true if adjacent pipes are to be considered vertices
    default boolean endPoint() { return true; };
}
