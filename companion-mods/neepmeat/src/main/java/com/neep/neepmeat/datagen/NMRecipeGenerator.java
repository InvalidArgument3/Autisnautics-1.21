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

package com.neep.neepmeat.datagen;

import com.neep.meatlib.datagen.MeatRecipeProvider;
import com.neep.meatlib.registry.BlockRegistry;
import com.neep.neepmeat.block.MetalScaffoldingBlock;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.init.NMItems;
import com.neep.neepmeat.transport.FluidTransport;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import java.util.function.Consumer;

public class NMRecipeGenerator extends MeatRecipeProvider
{
    public NMRecipeGenerator(FabricDataOutput output)
    {
        super(output);
    }

    @Override
    public String getName()
    {
        return "NEEPMeat recipes";
    }

    @Override
    public void buildRecipes(RecipeOutput exporter)
    {
        generateRecipes(exporter);
    }

    protected static void generateRecipes(RecipeOutput exporter)
    {
        MeatRecipeProvider.nineBlockStorageRecipes(exporter, RecipeCategory.BUILDING_BLOCKS, NMItems.MEAT_STEEL_INGOT, RecipeCategory.BUILDING_BLOCKS, NMBlocks.MEAT_STEEL_BLOCK);
        MeatRecipeProvider.nineBlockStorageRecipes(exporter, RecipeCategory.BUILDING_BLOCKS, NMItems.BROWNE_INGOT, RecipeCategory.BUILDING_BLOCKS, NMBlocks.BROWNE_BLOCK);
        MeatRecipeProvider.offerEightDyeingRecipe(exporter, NMBlocks.RUSTY_METAL_BLOCK, Items.DIRT, NMBlocks.POLISHED_METAL);
        MeatRecipeProvider.offerEightDyeingRecipe(exporter, NMBlocks.RUSTY_METAL_BLOCK, NMItems.PACKED_BROWNE, NMBlocks.POLISHED_METAL, "rusty_metal_from_browne");
        MeatRecipeProvider.offerEightDyeingRecipe(exporter, NMBlocks.DIRTY_RED_TILES, NMItems.BLOOD_BUBBLE, Blocks.TERRACOTTA);
        MeatRecipeProvider.offerEightDyeingRecipe(exporter, NMBlocks.DIRTY_WHITE_TILES, Blocks.DIRT, Blocks.TERRACOTTA);
        MeatRecipeProvider.offerEightDyeingRecipe(exporter, NMBlocks.BLUE_IRON_BLOCK, Items.BLUE_DYE, NMBlocks.POLISHED_METAL);

        MeatRecipeProvider.offerEightDyeingRecipe(exporter, NMBlocks.WHITE_ROUGH_CONCRETE, Items.SAND, Blocks.WHITE_CONCRETE);
        MeatRecipeProvider.offerEightDyeingRecipe(exporter, NMBlocks.GREY_ROUGH_CONCRETE, Items.SAND, Blocks.GRAY_CONCRETE);
        MeatRecipeProvider.offerEightDyeingRecipe(exporter, NMBlocks.YELLOW_ROUGH_CONCRETE, Items.SAND, Blocks.YELLOW_CONCRETE);
        MeatRecipeProvider.offerEightDyeingRecipe(exporter, NMBlocks.RED_ROUGH_CONCRETE, Items.SAND, Blocks.RED_CONCRETE);
        MeatRecipeProvider.offerEightDyeingRecipe(exporter, NMBlocks.BLUE_ROUGH_CONCRETE, Items.SAND, Blocks.BLUE_CONCRETE);

//        MeatRecipeProvider.offerEightDyeingRecipe(exporter, NMBlocks.BLUE_SCAFFOLD, Items.BLUE_DYE, NMTags.METAL_SCAFFOLDING);
//        MeatRecipeProvider.offerEightDyeingRecipe(exporter, NMBlocks.YELLOW_SCAFFOLD, Items.YELLOW_DYE, NMTags.METAL_SCAFFOLDING);


//
//        MeatRecipeProvider.offerEightDyeingRecipe(exporter, NMBlocks.DIRTY_YELLOW_TILES, NMFluids.BLOOD_BUCKET, NMBlocks.YELLOW_TILES);

        // This shouldn't be here.
        // (But it also shouldn't exist, so here's as bad as anywhere else.)
        BlockRegistry.REGISTERED_BLOCKS.stream()
                .filter(block -> block instanceof MetalScaffoldingBlock)
                .forEach(block -> ((MetalScaffoldingBlock) block).generateRecipe(exporter));

        for (var pipe : FluidTransport.COLOURED_FLUID_PIPES.values())
        {
            MeatRecipeProvider.offerEightDyeingRecipe(exporter, pipe, DyeItem.byColor(pipe.col.dyeCol), NMTags.FLUID_PIPES);
        }
    }
}
