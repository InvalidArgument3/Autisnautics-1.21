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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ItemPipe {
    boolean isConnected(Level world, BlockPos pos, BlockState state, Direction direction);

    static boolean isConnectedIn(Level world, BlockPos pos, BlockState state, Direction direction) {
        return state.getBlock() instanceof ItemPipe pipe && pipe.isConnected(world, pos, state, direction);
    }

    default boolean isConnected(BlockGetter world, BlockPos pos, BlockState state, Direction direction) {
        return world instanceof Level level && isConnected(level, pos, state, direction);
    }

}
