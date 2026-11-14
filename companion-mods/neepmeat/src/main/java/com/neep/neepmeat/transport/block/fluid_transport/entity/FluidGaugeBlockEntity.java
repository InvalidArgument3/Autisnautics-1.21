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

package com.neep.neepmeat.transport.block.fluid_transport.entity;
import com.neep.neepmeat.machine.transport.ConnectedPipeBlock;
import com.neep.neepmeat.machine.transport.DeferredRequest;
import com.neep.neepmeat.machine.transport.FilterEntry;
import com.neep.neepmeat.machine.transport.FilterPipeMenu;
import com.neep.neepmeat.machine.transport.NetworkCrafterBlockEntity;
import com.neep.neepmeat.machine.transport.StorageBusBlock;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.block.fluid_transport.FluidPipeBlock;
import com.neep.neepmeat.transport.block.fluid_transport.PumpBlock;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidPipeBlockEntity;
import com.neep.neepmeat.transport.block.fluid_transport.machine.PumpBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.FilterItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.ItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.MergePipeBlock;
import com.neep.neepmeat.transport.block.item_transport.PipeDriverBlock;
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity;
import com.neep.neepmeat.transport.item_network.PipeRouteFinder;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import com.neep.neepmeat.transport.block.fluid_transport.FluidGaugeBlock;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FluidGaugeBlockEntity<T> extends SyncableBlockEntity
{
    private final LazyBlockApiCache<Storage<T>, Direction> cache;
    private FluidVariant foundVariant = FluidVariant.BLANK;
    private int comparatorOutput = 0;

    public FluidGaugeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, BlockApiLookup<Storage<T>, Direction> lookup)
    {
        super(type, pos, state);

        Direction facing = getBlockState().getValue(FluidGaugeBlock.FACING);
        BlockPos offsetPos = pos.relative(facing);

        cache = LazyBlockApiCache.of(lookup, offsetPos, this::getLevel, () -> facing.getOpposite());
    }

    public void serverTick()
    {
        if (getLevel().getGameTime() % 4 == 0)
        {
            int prevOutput = comparatorOutput;

            Storage<T> storage = cache.find();
            if (storage != null)
            {
                comparatorOutput = StorageUtil.calculateComparatorOutput(storage);
            }
            else
            {
                comparatorOutput = 0;
            }

            if (comparatorOutput != prevOutput)
            {
                level.setBlockAndUpdate(worldPosition, FluidGaugeBlock.getLevelState(getBlockState(), comparatorOutput));
                ((FluidGaugeBlock<?>) getBlockState().getBlock()).updateNeighbors(level, worldPosition, getBlockState());
            }
        }
    }

    public int getOutput()
    {
        return comparatorOutput;
    }
}
