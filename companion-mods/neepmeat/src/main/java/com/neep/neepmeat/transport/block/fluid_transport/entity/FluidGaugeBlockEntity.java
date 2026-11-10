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
