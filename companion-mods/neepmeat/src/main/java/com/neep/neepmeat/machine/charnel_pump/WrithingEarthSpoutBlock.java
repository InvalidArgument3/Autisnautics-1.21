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

package com.neep.neepmeat.machine.charnel_pump;

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WrithingEarthSpoutBlock extends BaseBlock implements EntityBlock
{
    public WrithingEarthSpoutBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
    }

    public boolean autoGenDrop()
    {
        return false;
    }

    private boolean canSpread(BlockState state, Level world, BlockPos pos, int distance, boolean air)
    {
        if (!state.is(NMTags.CONTAMINATED_RUBBLE_REPLACABLE) && !(air && state.isAir()))
            return false;

        if (distance < 4)
            return true;

        BlockPos.MutableBlockPos mutable = pos.mutable();
        int neighbourCount = 0;

        for (Direction direction : Direction.values())
        {
            mutable.setWithOffset(pos, direction);
            BlockState neighbour = world.getBlockState(mutable);

            if (neighbour.is(NMBlocks.CONTAMINATED_RUBBLE) || neighbour.is(NMBlocks.WRITHING_EARTH_SPOUT))
                neighbourCount++;
        }
        return neighbourCount == 1;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
        return true;
    }

    boolean propagate(Level world, BlockPos pos, BlockState state, int distance, int maxDistance, RandomSource random, boolean air)
    {
        if (distance >= maxDistance)
            return false;

        List<Direction> directions = new ArrayList<>(List.of(Direction.values()));
        Collections.shuffle(directions);
        for (Direction direction : directions)
        {
            BlockPos newPos = pos.relative(direction);
            BlockState newState = world.getBlockState(newPos);

            if (newState.is(NMBlocks.WRITHING_EARTH_SPOUT) || newState.is(NMBlocks.CONTAMINATED_RUBBLE))
            {
                if (propagate(world, newPos, newState, distance + 1, maxDistance, random, air))
                    return true;
            }
            else if (canSpread(newState, world, newPos, distance, air))
            {
                world.setBlockAndUpdate(newPos, NMBlocks.CONTAMINATED_RUBBLE.defaultBlockState());
                return true;
            }
        }
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
    {
        propagate(world, pos, state, 0, 9, random, !world.getBlockState(pos.above()).is(NMBlocks.WELL_HEAD));
//        if (!canSurvive(state, world, pos))
//        {
//            world.setBlockState(pos, Blocks.DIRT.getDefaultState());
//        }
//        else
//        {
//            BlockState spreadState = NMBlocks.CONTAMINATED_DIRT.getDefaultState();
//
//            for(int i = 0; i < 4; ++i)
//            {
//                BlockPos spreadPos = pos.add(random.nextInt(7) - 3, random.nextInt(5) - 3, random.nextInt(7) - 3);
//                if (canSpread(world.getBlockState(spreadPos), world, spreadPos))
//                {
//                    world.setBlockState(spreadPos, spreadState);
//                }
//            }
//        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.WRITHING_EARTH_SPOUT.create(pos, state);
    }
}
