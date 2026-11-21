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

import com.neep.neepmeat.transport.ItemTransport;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.neepmeat.util.DFSFinder;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class RoutingNetworkDFSFinder extends DFSFinder<BlockApiCache<RoutingNetwork, Void>>
{
    private final Level world;

    public RoutingNetworkDFSFinder(Level world)
    {
        this.world = world;
    }

    @Override
    protected State processPos(BlockPos current, Direction fromDir)
    {
        ItemPipe fromPipe = ItemTransport.ITEM_PIPE_LOOKUP.find(world, current, fromDir);

        BlockApiCache<RoutingNetwork, Void> cache = BlockApiCache.create(RoutingNetwork.LOOKUP, (ServerLevel) world, current);
        if (cache.find(null) != null)
        {
            setResult(current, cache);
            return State.SUCCESS;
        }

        BlockPos.MutableBlockPos mutable = current.mutable();
        for (Direction direction : fromPipe.getConnections(world.getBlockState(current), null))
        {
            mutable.setWithOffset(current, direction);
            if (visited(mutable)) continue;

            ItemPipe offsetPipe = ItemTransport.ITEM_PIPE_LOOKUP.find(world, mutable, direction);
            if (offsetPipe != null)
            {
                setVisited(mutable);
                pushBlock(mutable, direction);
                return State.CONTINUE;
            }
        }

        popBlock();
        popDir();

        return State.CONTINUE;
    }

    public static RoutingNetwork getDriver(Level world, BlockPos pos)
    {
        return RoutingNetwork.LOOKUP.find(world, pos, null);
//        return world.getBlockEntity(pos, ItemTransport.PIPE_DRIVER_BE).orElse(null);
    }
}
