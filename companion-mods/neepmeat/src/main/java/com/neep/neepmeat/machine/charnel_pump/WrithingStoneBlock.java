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
import com.neep.neepmeat.init.NMBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class WrithingStoneBlock extends BaseBlock
{
    public WrithingStoneBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
        return true;
    }

    public boolean autoGenDrop()
    {
        return false;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
    {
        BlockState spreadState = NMBlocks.CONTAMINATED_RUBBLE.defaultBlockState();
        BlockState downState = NMBlocks.WRITHING_STONE.defaultBlockState();

        for (int i = 0; i < 4; ++i)
        {
            BlockPos spreadPos = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 4, random.nextInt(3) - 1);
            if (canSpread(world.getBlockState(spreadPos), world, spreadPos))
            {
                if (spreadPos.getX() == pos.getX() && spreadPos.getZ() == pos.getZ())
                    world.setBlockAndUpdate(spreadPos, downState);
                else
                    world.setBlockAndUpdate(spreadPos, spreadState);
            }
        }
    }

    private boolean canSpread(BlockState blockState, ServerLevel world, BlockPos spreadPos)
    {
//        return blockState.isAir();
        return blockState.is(BlockTags.STONE_ORE_REPLACEABLES) || blockState.is(BlockTags.DIRT);
    }
}
