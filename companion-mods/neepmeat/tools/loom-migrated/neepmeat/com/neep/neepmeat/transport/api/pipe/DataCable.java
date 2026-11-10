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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface DataCable
{
    default boolean connectInDirection(BlockGetter world, BlockPos pos, BlockState state, Direction direction)
    {
        return true;
    }

    /**
     * @param outgoing Whether this block is the origin of the read or write.
     */
    default boolean isConnected(Level world, BlockPos pos, BlockState state, Direction direction, boolean outgoing)
    {
        return true;
    }

    /**
     * @return Whether searches should check that the next block is connected to this one... erm...
     * It's so that full blocks that are permanently connected in all directions honour the connections of adjacent data cables.
     * Return false for things like {@link com.neep.neepmeat.block.DataCableBlock}
     */
    default boolean checkAdjacentConnection()
    {
        return true;
    }
}
