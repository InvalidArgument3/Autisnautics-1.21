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

import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.neepmeat.transport.api.pipe.ItemPipeConnectable;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class RoutingNetworkCache
{
    protected BlockApiCache<RoutingNetwork, Void> networkCache;
    protected long networkVersion;

    /// Call on neighbour update
    public void neighbourUpdate(Level world, BlockPos sourcePos, Block sourceBlock)
    {
        // sourceBlock is the previous occupant of sourcePos
        // It will be air if the block at sourcePos has just been placed
        // It will be a potential item pipe if the block at sourcePos has just been broken
        Block block = world.getBlockState(sourcePos).getBlock();

        // Assumes that ItemPipeConnectable blocks might have block entities that provide a RoutablePipe

        // Need to check both sourceBlock and the current block as
        if (block instanceof ItemPipe || block instanceof ItemPipeConnectable
                || sourceBlock instanceof ItemPipe || sourceBlock instanceof ItemPipeConnectable)
        {
            update();
        }
    }

    /// Call whenever a change to the network topology occurs near this pipe.
    /// Does not need to be called when the change originates in a non-pipe, or when a local connection changes.
    /// Call when the block is broken or when a neighbouring pipe is added or removed or something.
    public void update()
    {
        if (networkCache != null)
        {
            RoutingNetwork network = networkCache.find(null);
            if (network != null && network.getVersion() == this.networkVersion)
            {
                network.invalidate();
            }
        }
    }

    public boolean isValid()
    {
        RoutingNetwork network = networkCache.find(null);
        if (network != null)
        {
            return network.getVersion() == networkVersion;
        }
        return true;
    }

    public void setNetwork(ServerLevel world, BlockPos pos)
    {
        this.networkCache = BlockApiCache.create(RoutingNetwork.LOOKUP, world, pos);
        RoutingNetwork network = networkCache.find(null);

        if (network != null)
            this.networkVersion = network.getVersion();
    }

    @Nullable
    public RoutingNetwork getNetwork()
    {
        return networkCache != null ? networkCache.find(null) : null;
    }
}
