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
import com.neep.neepmeat.api.ContaminationUtil;
import com.neep.neepmeat.util.IterateRandomly;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class ContaminatedRubbleBlock extends BaseBlock implements ContaminationBlock
{
    private final int level;

    public ContaminatedRubbleBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings, int level)
    {
        super(ctx, itemSettings, settings);
        this.level = level;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
    {
        BlockPos.MutableBlockPos mutable = pos.mutable();
        for (int i = 0; i < 4; i++)
        {
            mutable.setWithOffset(pos, random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
            BlockState offsetState = world.getBlockState(mutable);
            ContaminationUtil.contaminate(world, mutable, offsetState, level - 1, 1);
        }

        super.randomTick(state, world, pos, random);
    }

    @Override
    public int getLevel(BlockState state)
    {
        return level;
    }
}
