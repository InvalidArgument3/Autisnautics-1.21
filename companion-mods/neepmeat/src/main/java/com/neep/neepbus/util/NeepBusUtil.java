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

package com.neep.neepbus.util;

import com.neep.neepbus.block.NeepBusProvider;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayDeque;
import java.util.Queue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class NeepBusUtil
{
    public static String formatValue(int value)
    {
        return String.valueOf(value);
    }

    public static void updateImmediateNeighbours(Level world, BlockPos pos, BlockState state)
    {
        BlockPos.MutableBlockPos mutable = pos.mutable();
        for (Direction direction : Direction.values())
        {
            mutable.setWithOffset(pos, direction);

            if (world.getBlockState(mutable).getBlock() instanceof NeepBusProvider provider)
            {
                provider.networkChanged(world, mutable, pos);
            }
        }
    }

    public static void floodUpdate(Level world, BlockPos pos)
    {
        // BFS #1381641

        LongSet visited = new LongOpenHashSet();
        Queue<BlockPos> queue = new ArrayDeque<>();

        visited.add(pos.asLong());
        queue.add(pos);

        while (!queue.isEmpty())
        {
            BlockPos current = queue.poll();

            BlockPos.MutableBlockPos mutable = current.mutable();

            for (Direction direction : Direction.values())
            {
                mutable.setWithOffset(current, direction);

                if (!visited.contains(mutable.asLong()))
                {
                    visited.add(mutable.asLong());
                    BlockState offsetState = world.getBlockState(mutable);

                    if (offsetState.getBlock() instanceof DataCable)
                    {
                        queue.add(mutable.immutable());
                    }

                    if (offsetState.getBlock() instanceof NeepBusProvider provider)
                    {
                        provider.networkChanged(world, mutable, pos);
                    }
                }
            }
        }
    }
}
