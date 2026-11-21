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

package com.neep.meatweapons.datagen;

import com.neep.meatlib.datagen.MeatRecipeProvider;
import com.neep.meatweapons.MWItems;
import com.neep.meatweapons.init.MWBlocks;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.init.NMItems;
import com.neep.meatlib.datagen.FabricDataOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import java.util.function.Consumer;

public class MWRecipeGenerator extends MeatRecipeProvider
{
    public MWRecipeGenerator(FabricDataOutput output)
    {
        super(output);
    }

    @Override
    public String getName()
    {
        return "MeatWeapons recipes";
    }

    @Override
    public void buildRecipes(RecipeOutput exporter)
    {
        generateRecipes(exporter);
    }

    protected static void generateRecipes(RecipeOutput exporter)
    {
        // Barrels
        new ShapedRecipeBuilder(RecipeCategory.MISC, MWItems.IRON_BARREL, 1)
                .unlockedBy("has", com.neep.meatlib.datagen.FabricRecipeProvider.has(Items.IRON_INGOT))
                .define('n', Items.IRON_NUGGET)
                .define('i', Items.IRON_INGOT)
                .pattern(" ni")
                .pattern("nin")
                .pattern("in ")
                .save(exporter);
        new ShapedRecipeBuilder(RecipeCategory.MISC, MWItems.MEAT_STEEL_BARREL, 1)
                .unlockedBy("has", com.neep.meatlib.datagen.FabricRecipeProvider.has(NMItems.MEAT_STEEL_INGOT))
                .define('n', NMItems.MEAT_SCRAP)
                .define('i', NMItems.MEAT_STEEL_INGOT)
                .pattern(" ni")
                .pattern("nin")
                .pattern("in ")
                .save(exporter);

        // Base modules
        new ShapedRecipeBuilder(RecipeCategory.TOOLS, MWItems.MEATGUN_PISTOL, 1)
                .unlockedBy("has", com.neep.meatlib.datagen.FabricRecipeProvider.has(NMItems.MEAT_STEEL_INGOT))
                .define('n', Items.IRON_NUGGET)
                .define('m', NMItems.MEAT_STEEL_INGOT)
                .define('c', NMItems.CONTROL_UNIT)
                .define('i', NMItems.INTERNAL_COMPONENTS)
                .pattern("nnn")
                .pattern("imm")
                .pattern("nnc")
                .save(exporter);
        new ShapedRecipeBuilder(RecipeCategory.TOOLS, MWItems.MEATGUN_STAFF, 1)
                .unlockedBy("has", com.neep.meatlib.datagen.FabricRecipeProvider.has(NMItems.MEAT_STEEL_INGOT))
                .define('m', NMItems.MEAT_STEEL_INGOT)
                .define('c', NMItems.CONTROL_UNIT)
                .define('i', NMItems.INTERNAL_COMPONENTS)
                .pattern("ic")
                .pattern("m ")
                .pattern("m ")
                .save(exporter);

        // Tinker table
        new ShapedRecipeBuilder(RecipeCategory.MISC, MWBlocks.TINKER_TABLE.asItem(), 1)
                .unlockedBy("has", com.neep.meatlib.datagen.FabricRecipeProvider.has(NMItems.MEAT_STEEL_INGOT))
                .define('n', Items.IRON_NUGGET)
                .define('m', Items.IRON_INGOT)
                .define('b', NMItems.BIOELECTRIC_ORGAN)
                .define('i', NMItems.INTERNAL_COMPONENTS)
                .define('t', Blocks.CRAFTING_TABLE.asItem())
                .pattern("nnn")
                .pattern("ibi")
                .pattern("mtm")
                .save(exporter);

        // Modules
        new ShapedRecipeBuilder(RecipeCategory.MISC, MWItems.PISTOL, 1)
                .unlockedBy("has", com.neep.meatlib.datagen.FabricRecipeProvider.has(Items.IRON_INGOT))
                .define('b', MWItems.IRON_BARREL)
                .define('i', Items.IRON_INGOT)
                .pattern("  ")
                .pattern("bi")
                .pattern(" i")
                .save(exporter);
        new ShapedRecipeBuilder(RecipeCategory.MISC, MWItems.CHUGGER, 1)
                .unlockedBy("has", com.neep.meatlib.datagen.FabricRecipeProvider.has(NMItems.MEAT_STEEL_INGOT))
                .define('m', NMItems.MEAT_STEEL_INGOT)
                .define('b', MWItems.IRON_BARREL)
                .define('i', Items.IRON_NUGGET)
                .pattern("ii")
                .pattern("bm")
                .pattern("mm")
                .save(exporter);
        new ShapedRecipeBuilder(RecipeCategory.MISC, MWItems.GRENADE_LAUNCHER, 1)
                .unlockedBy("has", com.neep.meatlib.datagen.FabricRecipeProvider.has(NMItems.MEAT_STEEL_INGOT))
                .define('m', NMItems.MEAT_STEEL_INGOT)
                .define('b', MWItems.IRON_BARREL)
                .define('i', Items.IRON_NUGGET)
                .define('c', NMItems.CONTRACTILE_ACTUATOR)
                .pattern("ii ")
                .pattern("bbc")
                .pattern("mm ")
                .save(exporter);
        new ShapedRecipeBuilder(RecipeCategory.MISC, MWItems.UNDERBARREL, 1)
                .unlockedBy("has", com.neep.meatlib.datagen.FabricRecipeProvider.has(Items.IRON_INGOT))
                .define('m', Items.IRON_INGOT)
                .define('b', NMItems.INTERNAL_COMPONENTS)
                .define('i', Items.IRON_NUGGET)
                .pattern(" i")
                .pattern("bm")
                .pattern("bm")
                .save(exporter);
        new ShapedRecipeBuilder(RecipeCategory.MISC, MWItems.DOUBLE_CAROUSEL, 1)
                .unlockedBy("has", com.neep.meatlib.datagen.FabricRecipeProvider.has(NMItems.MEAT_STEEL_INGOT))
                .define('m', NMItems.MEAT_STEEL_INGOT)
                .define('b', NMItems.INTERNAL_COMPONENTS)
                .define('i', Items.IRON_INGOT)
                .pattern("bi")
                .pattern(" m")
                .pattern("bi")
                .save(exporter);
        new ShapedRecipeBuilder(RecipeCategory.MISC, MWItems.TRIPLE_CAROUSEL, 1)
                .unlockedBy("has", com.neep.meatlib.datagen.FabricRecipeProvider.has(NMItems.MEAT_STEEL_INGOT))
                .define('m', NMItems.MEAT_STEEL_INGOT)
                .define('b', NMItems.INTERNAL_COMPONENTS)
                .define('c', NMItems.CONTRACTILE_ACTUATOR)
                .define('i', Items.IRON_INGOT)
                .pattern("bic")
                .pattern("cmb")
                .pattern("bic")
                .save(exporter);

        new ShapedRecipeBuilder(RecipeCategory.MISC, MWItems.HALBERD, 1)
                .unlockedBy("has", com.neep.meatlib.datagen.FabricRecipeProvider.has(NMItems.MEAT_STEEL_INGOT))
                .define('d', Items.DIAMOND)
                .define('m', NMItems.MEAT_STEEL_INGOT)
                .pattern("dm ")
                .pattern("dmm")
                .pattern("dm ")
                .save(exporter);
        new ShapedRecipeBuilder(RecipeCategory.MISC, MWItems.SHORT_PHAGE_RAY, 1)
                .unlockedBy("has", com.neep.meatlib.datagen.FabricRecipeProvider.has(NMItems.MEAT_STEEL_INGOT))
                .define('b', NMBlocks.LINEAR_OSCILLATOR.asItem())
                .define('o', NMItems.HUNGRY_HETEROSTRUCTURE)
                .define('m', NMItems.MEAT_STEEL_INGOT)
                .define('a', MWItems.MEAT_STEEL_BARREL)
                .pattern("mm ")
                .pattern("boa")
                .pattern("mm ")
                .save(exporter);
        new ShapedRecipeBuilder(RecipeCategory.MISC, MWItems.PHAGE_RAY_SPEED_MODIFIER, 1)
                .unlockedBy("has", com.neep.meatlib.datagen.FabricRecipeProvider.has(NMItems.MEAT_STEEL_INGOT))
                .define('g', NMBlocks.REINFORCED_GLASS.asItem())
                .define('o', NMItems.HUNGRY_HETEROSTRUCTURE)
                .define('m', NMItems.MEAT_STEEL_INGOT)
                .define('n', Items.IRON_NUGGET)
                .pattern("ngn")
                .pattern("non")
                .pattern("nmn")
                .save(exporter);
    }
}
