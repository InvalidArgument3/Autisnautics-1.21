/*
 * Copyright (c) MeatWheeze 2026.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.transport.block.item_transport.entity;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.plc.robot.PLCActuator;
import com.neep.neepmeat.transport.ItemTransport;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.neepmeat.transport.block.item_transport.PipeDriverBlock;
import com.neep.neepmeat.transport.block.item_transport.RoutingNetworkImpl;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.ResourceAmount;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PipeDriverBlockEntity extends SyncableBlockEntity implements PLCActuator, PLCActuator.Provider
{
    protected final RoutingNetworkImpl network = new RoutingNetworkImpl(worldPosition, () -> (ServerLevel) this.getLevel());

    public PipeDriverBlockEntity(BlockPos pos, BlockState state)
    {
        this(ItemTransport.PIPE_DRIVER_BE, pos, state);
    }

    public PipeDriverBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public RoutingNetwork getNetwork(Void ignored)
    {
        return network;
    }

    public static void emitUpdate(ServerLevel world, BlockPos origin, int maxDepth)
    {
        Set<Long> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>(20);
        int depth = 0;

        queue.add(origin);
        while (!queue.isEmpty() && depth < maxDepth)
        {
            ++depth;
            BlockPos current = queue.poll();

            BlockPos.MutableBlockPos mutable = current.mutable();
            for (Direction direction : Direction.values())
            {
                mutable.setWithOffset(current, direction);

                if (visited.contains(mutable.asLong())) continue;
                visited.add(mutable.asLong());

                RoutingNetwork net = RoutingNetwork.LOOKUP.find(world, mutable, null);
                if (net != null)
                {
                    net.update();
                    return;
                }

                ItemPipe nextPipe = ItemTransport.ITEM_PIPE_LOOKUP.find(world, mutable, direction.getOpposite());
                if (nextPipe != null)
                {
                    queue.add(mutable.immutable());
                }
            }
        }
    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, PipeDriverBlockEntity be)
    {
        if (be.network.needsUpdate())
        {
            be.network.update();
            world.setBlockAndUpdate(pos, state.setValue(PipeDriverBlock.VALID, be.network.isValid()));
        }

        be.network.serverTick();

        if (world.getGameTime() % 2 == 0)
        {
            boolean hasRequests = !be.network.getActiveRequests().isEmpty();
            if (be.getBlockState().getValue(PipeDriverBlock.HAS_REQUESTS) != hasRequests)
            {
                world.setBlock(pos, state.setValue(PipeDriverBlock.HAS_REQUESTS, hasRequests), PipeDriverBlock.UPDATE_NEIGHBORS);
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        nbt.put("network", network.writeNbt(new CompoundTag()));
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.network.readNbt(nbt.getCompound("network"));
    }

    @Override
    public PLCActuator getPlcActuator()
    {
        return this;
    }

    @Override
    public BlockPos getBasePos()
    {
        return worldPosition;
    }

    @Override
    public void spawnItem(@Nullable ResourceAmount<ItemVariant> stored)
    {
        if (stored == null)
            return;
        Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.25, worldPosition.getZ() + 0.5, stored.resource().toStack((int) stored.amount()));
    }

    @Override
    public boolean actuatorRemoved()
    {
        return isRemoved();
    }
}
