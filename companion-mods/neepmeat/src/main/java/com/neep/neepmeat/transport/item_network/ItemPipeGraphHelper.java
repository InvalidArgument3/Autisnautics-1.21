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

import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/// Holds the methods for wrangling simplifying pipe graphs.
/// Conceptually different to a vertex, but might be implemented by a vertex.
public interface ItemPipeGraphHelper
{
    void prepareForRemoval(BlockState state, BlockState newState);
    void updateConnectionChange(BlockState state, BlockState newState);
    void updateHiddenConnections(BlockState state);

    static Optional<ItemPipeGraphHelper> find(ServerLevel world, BlockPos pos, BlockState state)
    {
        // Here, the vertex implements this interface, but this may change
        if (state.getBlock() instanceof ItemPipe ip && ip.getPipeVertex(world, pos, state) instanceof ItemPipeVertex vertex)
        {
            return Optional.of(vertex);
        }
        return Optional.empty();
    }

}
