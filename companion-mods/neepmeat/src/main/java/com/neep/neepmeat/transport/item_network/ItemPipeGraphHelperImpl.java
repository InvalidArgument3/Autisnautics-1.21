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

import com.neep.meatlib.util.PipeConnectionsC;
import com.neep.meatlib.util.graph.ConnectablePipe;
import com.neep.neepmeat.util.PipeGraphUtil;
import com.neep.neepmeat.api.storage.WorldSupplier;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.meatlib.util.PipeConnections;
import it.unimi.dsi.fastutil.Pair;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ItemPipeGraphHelperImpl implements ItemPipeGraphHelper, ItemPipeVertex
{
    // ehhhhhhhhhhhhhh
    private final ItemPipeVertex vertex = this;

    protected final WorldSupplier world;
    protected BlockState cachedState1;
    protected final ConnectablePipe pipe;
    private final BlockPos pos;

    protected PipeConnectionsC connections = new PipeConnections();

    protected ItemPipeGraphHelperImpl(WorldSupplier world, Supplier<BlockState> getCachedState, ConnectablePipe pipe, BlockPos pos)
    {
        this.world = world;
        this.cachedState1 = getCachedState.get();
        this.connections = pipe.getConnections(cachedState1);
        this.pipe = pipe;
        this.pos = pos;
    }

    @Override
    public void prepareForRemoval(BlockState state, BlockState newState)
    {
        vertex.clearEdges();

        PipeGraphUtil.eraseHiddenFromNeighbours(world.get(), pos, state, pipe, ItemPipe::findItemPipe);
    }

    @Override
    public void updateConnectionChange(BlockState state, BlockState newState)
    {
        setCachedState(newState);

        int connections = pipe.countConnections(newState);

        // If this is a newly created pipe end, we need to break the previous connection.
        if (vertex.canSimplify() && connections == 1)
        {
            vertex.clearEdges();

            // Using a loop to be safe, but this should only happen once.
            for (Direction direction : pipe.getConnections(getCachedState()).directions())
            {
                var adjacent = findNextVertex(pos, direction);
                if (adjacent != null)
                {
                    adjacent.first().setAdjVertex(adjacent.second().ordinal(), null);
                    PipeGraphUtil.jankParticles(world.get(), adjacent.first(), false);
                    setAdjVertex(direction.ordinal(), null);
                }
            }
        }
        else
        {
            updateHiddenConnections(newState);
        }
    }

    @Override
    public void updateHiddenConnections(BlockState newState)
    {
        setCachedState(newState);

        vertex.clearEdges();

        int connections = pipe.countConnections(newState);
        if (connections > 2 || !vertex.canSimplify())
        {
            findAdjacent();
        }
        else if (connections == 2)
        {
            PipeGraphUtil.linkVertices(world.get(), pos, getCachedState(), pipe, ItemPipeVertex::find, ItemPipe::findItemPipe);
        }
    }

    // For use if this pipe is a vertex. Performs a BFS at each connection to find the closest vertices.
    private void findAdjacent()
    {
        for (Direction direction : pipe.getConnections(getCachedState()).directions())
        {
            var adjacent = findNextVertex(pos, direction);
            if (adjacent != null)
            {
                vertex.setAdjVertex(direction.get3DDataValue(), adjacent.first());
                adjacent.first().setAdjVertex(adjacent.second().get3DDataValue(), vertex);
                PipeGraphUtil.jankParticles(world.get(), adjacent.first(), true);
            }
        }
    }

    private Pair<ItemPipeVertex, Direction> findNextVertex(BlockPos pos, Direction out)
    {
        return PipeGraphUtil.findNextVertex(world.get(), pos, out, ItemPipeVertex::find, ItemPipe::findItemPipe);
    }

    @Override
    public BlockPos getPos()
    {
        return pos;
    }

    public BlockState getCachedState()
    {
        return cachedState1;
    }

    public void setCachedState(BlockState state)
    {
        this.cachedState1 = state;
        this.connections = pipe.getConnections(cachedState1);
    }
}
