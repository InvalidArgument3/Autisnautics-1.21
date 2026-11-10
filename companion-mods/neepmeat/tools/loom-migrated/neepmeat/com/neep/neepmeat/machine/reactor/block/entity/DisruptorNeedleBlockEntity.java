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

package com.neep.neepmeat.machine.reactor.block.entity;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.machine.reactor.disruptor.DisruptorNeedle;
import com.neep.neepmeat.machine.reactor.disruptor.DisruptorNeedleBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DisruptorNeedleBlockEntity extends SyncableBlockEntity implements DisruptorNeedle
{
//    @Nullable private ReactionCoreBlockEntity core;

    public DisruptorNeedleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void setController(BlockPos pos)
    {
//        world.setBlockState(pos, getCachedState().
    }

    @Override
    public boolean componentRemoved()
    {
        return isRemoved();
    }

    @Nullable
    public ReactionCoreBlockEntity findCore()
    {
//        core = null;

        BlockPos.MutableBlockPos mutable = worldPosition.mutable();
        for (Direction direction : Direction.values())
        {
            mutable.setWithOffset(worldPosition, direction);

            // TODO: Account for other valid blocks
            if (level.getBlockEntity(mutable) instanceof ReactionCoreBlockEntity found)
            {
//                this.core = found;
                return found;
            }
        }
        return null;
    }

    @Override
    public void updateState(boolean active)
    {
        boolean prevActive = getBlockState().getValue(DisruptorNeedleBlock.ACTIVE);

        if (prevActive != active)
        {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(DisruptorNeedleBlock.ACTIVE, active));
        }
    }

//    public void onStateReplaced()
//    {
//        if (core != null)
//        {
//            core = null;
//        }
//    }
}
