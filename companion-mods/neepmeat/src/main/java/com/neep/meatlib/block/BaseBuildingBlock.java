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

import com.neep.meatlib.datagen.MeatRecipeProvider;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import java.util.function.Consumer;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class BaseBuildingBlock extends Block implements MeatlibBlock
{
    private final Item blockItem;

    public final BaseSlabBlock slab;
    public final BaseStairsBlock stairs;
    public MeatlibBlock wall = null;

    public BaseBuildingBlock(RegistrationContext ctx, boolean makeWall, MeatlibBlockSettings settings)
    {
        this(ctx, makeWall, ItemSettings.block(), settings);
    }

    public BaseBuildingBlock(RegistrationContext ctx, boolean makeWall, ItemSettings itemSettings, MeatlibBlockSettings settings)
    {
        super(settings);

        this.stairs = new BaseStairsBlock(ctx, this.defaultBlockState(), ItemSettings.block(), settings);
        ctx.append(this, stairs, s -> s + "_stairs");

        this.slab = new BaseSlabBlock(ctx, this.defaultBlockState(), ItemSettings.block(), settings);
        ctx.append(this, slab, s -> s + "_slab");

        if (makeWall)
        {
            this.wall = makeWall(ctx);
            ctx.append(this, wall, s -> s + "_wall");
        }

        this.blockItem = itemSettings.create(this, ctx);
    }

    protected MeatlibBlock makeWall(RegistrationContext ctx)
    {
        return new BaseWallBlock(ctx, ItemSettings.block(), properties);
    }

    public void generateRecipes(RecipeOutput exporter)
    {
        MeatRecipeProvider.slab(exporter, RecipeCategory.BUILDING_BLOCKS, this.slab, this);
        RecipeProvider.stonecutterResultFromBase(exporter, RecipeCategory.BUILDING_BLOCKS, this.slab, this, 2);
        MeatRecipeProvider.offerStairsRecipe(exporter, this.stairs, this);
        RecipeProvider.stonecutterResultFromBase(exporter, RecipeCategory.BUILDING_BLOCKS, this.stairs, this);
        if (wall != null)
        {
            MeatRecipeProvider.wall(exporter, RecipeCategory.BUILDING_BLOCKS, this.wall, this);
            RecipeProvider.stonecutterResultFromBase(exporter, RecipeCategory.BUILDING_BLOCKS, this.wall, this);
        }
    }
}