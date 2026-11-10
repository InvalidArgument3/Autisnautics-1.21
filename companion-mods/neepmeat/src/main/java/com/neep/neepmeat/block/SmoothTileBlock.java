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

package com.neep.neepmeat.block;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.block.PaintedBlockCollection;
import com.neep.meatlib.datagen.MeatRecipeProvider;
import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.item.MeatlibItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NMItemGroups;
import com.neep.neepmeat.datagen.tag.NMTags;
import java.util.function.Consumer;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class SmoothTileBlock extends PaintedBlockCollection.PaintedBlock implements MeatlibBlock
{
    public SmoothTileBlock(RegistrationContext ctx, DyeColor col, Properties settings)
    {
        super(ctx, col, settings);
    }

    @Override
    protected BlockItem makeItem(RegistrationContext ctx)
    {
        return new Item(this, ctx, ItemSettings.block());
    }

    public void generateRecipe(Consumer<FinishedRecipe> exporter)
    {
        MeatRecipeProvider.offerEightDyeingRecipe(exporter, "_dyeing", this, DyeItem.byColor(getCol()), NMTags.SMOOTH_TILE);
        MeatRecipeProvider.offerEightDyeingRecipe(exporter, this, DyeItem.byColor(getCol()), Blocks.SMOOTH_STONE);
    }

    private static class Item extends BaseBlockItem
    {
        public Item(Block block, RegistrationContext ctx, ItemSettings itemSettings)
        {
            super(block, ctx, itemSettings, new MeatlibItemSettings().tags(NMTags.SMOOTH_TILE).group(NMItemGroups.BUILDING));
        }
    }
}
