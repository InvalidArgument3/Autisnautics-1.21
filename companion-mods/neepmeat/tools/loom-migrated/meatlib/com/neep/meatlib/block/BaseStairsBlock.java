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

package com.neep.meatlib.block;

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BaseStairsBlock extends StairBlock implements MeatlibBlock
{
    protected BlockItem blockItem;
    private Block baseBlock;

    public BaseStairsBlock(RegistrationContext ctx, BlockState baseBlockState, ItemSettings itemSettings, MeatlibBlockSettings settings)
    {
        super(baseBlockState, settings.addTags(BlockTags.STAIRS));
//        System.out.println(this.hashCode() + ", " + baseBlockState.getBlock().getClass());
        this.baseBlock = baseBlockState.getBlock();
        this.blockItem = itemSettings.create(this, ctx);
    }

    @Override
    public String toString()
    {
        return super.toString() + baseBlock.getClass();
    }
}
