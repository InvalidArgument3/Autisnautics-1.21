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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.transport.blood_network.BloodNetwork;
import com.neep.neepmeat.transport.blood_network.BloodNetworkManager;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
import java.util.Set;

public interface VascularConduit
{
    BlockApiLookup<VascularConduit, Void> LOOKUP = BlockApiLookup.get(
            ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "vascular_conduit"),
            VascularConduit.class, Void.class);

    static VascularConduit find(Level world, BlockPos pos, BlockState state)
    {
        if (state.getBlock() instanceof VascularConduit conduit)
            return conduit;

        return LOOKUP.find(world, pos, null);
    }

    static boolean isConnected(BlockAndTintGetter blockView, BlockPos pos, BlockState state, Direction direction)
    {
        if (state.getBlock() instanceof VascularConduit conduit)
        {
            return conduit.isConnectedIn(blockView, pos, state, direction);
        }
        return false;
    }

    default void updatePosition(Level world, BlockPos pos, BlockState state, VascularConduitEntity.UpdateReason reason)
    {
        if (world.isClientSide())
            return;

        switch (reason)
        {
            case ADDED ->
            {
                List<BloodNetwork> adjNetworks = Lists.newArrayList();
                BlockPos.MutableBlockPos mutable = pos.mutable();
                for (Direction direction : Direction.values())
                {
                    if (!isConnectedIn(world, pos, state, direction)) continue;

                    mutable.setWithOffset(pos, direction);
                    var network = BloodNetwork.find(world, mutable);
                    if (network != null)
                    {
                        adjNetworks.add(network);
                    }
                }

                if (adjNetworks.isEmpty())
                {
                    var network = BloodNetworkManager.get(world).create(pos);
                    network.add(pos, getEntity(world, pos, state));
//                    network.update(pos, VascularConduitEntity.UpdateReason.ADDED);
                }
                else if (adjNetworks.size() == 1)
                {
                    adjNetworks.get(0).add(pos, getEntity(world, pos, state));
                }
                else
                {
                    adjNetworks.get(0).add(pos, getEntity(world, pos, state));
                    adjNetworks.get(0).merge(adjNetworks);
                }
            }
            case REMOVED ->
            {
                var original = BloodNetwork.find(world, pos);
                if (original != null)
                {
                    original.remove(pos, getEntity(world, pos, state));
                }

                Set<BloodNetwork> updatedNetworks = Sets.newHashSet();
                BlockPos.MutableBlockPos mutable = pos.mutable();
                for (Direction direction : Direction.values())
                {
                    if (!isConnectedIn(world, pos, state, direction)) continue;

                    mutable.setWithOffset(pos, direction);

                    // Detect conduits with a null network
                    var conduit = VascularConduit.find(world, mutable, world.getBlockState(mutable));
                    var network = BloodNetwork.find(world, mutable);
                    if (conduit != null)
                    {
                        if (network != null)
                        {
                            if (!updatedNetworks.contains(network))
                            {
                                updatedNetworks.add(network);
                                network.rebuild(mutable.immutable(), reason);
                            }
                        }
                        else
                        {
                            // If there is a connected pipe with a null network, it must have been disowned.
                            var newNetwork = BloodNetworkManager.get(world).create(mutable);
                            newNetwork.rebuild(mutable.immutable(), reason);
                        }
                    }
                }
            }
            case CHANGED ->
            {
                var network = BloodNetwork.find(world, pos);
                if (network != null)
                {
                    network.update(pos, getEntity(world, pos, state));
                }
            }
        }
    }

    default boolean isConnectedIn(BlockGetter world, BlockPos pos, BlockState state, Direction direction)
    {
        if (state.getBlock() instanceof PrecisePipeBlock)
        {
            return state.getValue(PrecisePipeBlock.DIR_TO_CONNECTION.get(direction)).isConnected();
        }
        return false;
    }

    VascularConduitEntity getEntity(Level world, BlockPos pos, BlockState state);
}
