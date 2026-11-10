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

package com.neep.neepmeat.machine.living_rock;

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class VerrucousStoneBlock extends BaseBlock
{
    public static final BooleanProperty OUTER = BooleanProperty.create("outer");

    public VerrucousStoneBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings.randomTicks());
        registerDefaultState(defaultBlockState().setValue(OUTER, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(OUTER);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
    {
        super.randomTick(state, world, pos, random);
        BlockPos.MutableBlockPos mutable = pos.mutable();
        for (Direction direction : Direction.values())
        {
            mutable.setWithOffset(pos, direction);

            if (!world.getBlockState(mutable)
                    .isFaceSturdy(world, pos, direction.getOpposite(), SupportType.FULL))
            {
                world.setBlockAndUpdate(pos, state.setValue(OUTER, true));
                break;
            }
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
        return !state.getValue(OUTER);
    }
}
