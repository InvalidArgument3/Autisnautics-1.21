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

package com.neep.meatlib.datagen;

import com.neep.meatlib.block.BaseBuildingBlock;
import com.neep.meatlib.block.PaintedBlockCollection;
import com.neep.meatlib.registry.BlockRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MeatRecipeProvider extends FabricRecipeProvider
{
    public MeatRecipeProvider(FabricDataOutput output)
    {
        super(output);
    }

    @Override
    public String getName()
    {
        return "MeatLib automatic recipes";
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> exporter)
    {
        // This is a bit stupid
        BlockRegistry.REGISTERED_BLOCKS.stream()
                .filter(block -> block instanceof BaseBuildingBlock)
                .forEach(block -> ((BaseBuildingBlock) block).generateRecipes(exporter));

        BlockRegistry.REGISTERED_BLOCKS.stream()
                .filter(block -> block instanceof PaintedBlockCollection.PaintedBlock)
                .forEach(block -> ((PaintedBlockCollection.PaintedBlock) block).generateRecipe(exporter));

        EXPORTER_CONSUMERS.forEach(consumer -> consumer.accept(exporter));
    }

    public static void offerStairsRecipe(Consumer<FinishedRecipe> exporter, ItemLike output, ItemLike input)
    {
        RecipeProvider.stairBuilder(output, Ingredient.of(input)).unlockedBy(RecipeProvider.getHasName(input), RecipeProvider.has(input)).save(exporter);
    }

    public static void offerEightDyeingRecipe(Consumer<FinishedRecipe> exporter, ItemLike output, ItemLike dye, ItemLike input)
    {
        createEightDyeingRecipe(output, Ingredient.of(dye), Ingredient.of(input))
                .unlockedBy(RecipeProvider.getHasName(input), RecipeProvider.has(input))
                .unlockedBy(RecipeProvider.getHasName(dye), RecipeProvider.has(dye))
                .save(exporter);
    }

    public static void offerEightDyeingRecipe(Consumer<FinishedRecipe> exporter, ItemLike output, ItemLike dye, ItemLike input, String path)
    {
        createEightDyeingRecipe(output, Ingredient.of(dye), Ingredient.of(input))
                .unlockedBy(RecipeProvider.getHasName(input), RecipeProvider.has(input))
                .unlockedBy(RecipeProvider.getHasName(dye), RecipeProvider.has(dye))
                .save(exporter, path);
    }

    public static void offerEightDyeingRecipe(Consumer<FinishedRecipe> exporter, ItemLike output, ItemLike dye, TagKey<Item> input)
    {
        createEightDyeingRecipe(output, Ingredient.of(dye), Ingredient.of(input))
                .unlockedBy("has_dye", RecipeProvider.has(input))
                .unlockedBy(RecipeProvider.getHasName(dye), RecipeProvider.has(dye))
                .save(exporter);
    }

    public static void offerEightDyeingRecipe(Consumer<FinishedRecipe> exporter, String suffix, ItemLike output, ItemLike dye, TagKey<Item> input)
    {
        ResourceLocation outputId = BuiltInRegistries.ITEM.getKey(output.asItem());
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(outputId.getNamespace(), outputId.getPath() + suffix);
        createEightDyeingRecipe(output, Ingredient.of(dye), Ingredient.of(input))
                .unlockedBy("has_dye", RecipeProvider.has(input))
                .unlockedBy(RecipeProvider.getHasName(dye), RecipeProvider.has(dye))
                .save(exporter, id);
    }

    public static RecipeBuilder createEightDyeingRecipe(ItemLike output, Ingredient dye, Ingredient input)
    {
        return ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, output, 8).define('i', input).define('d', dye).pattern("iii").pattern("idi").pattern("iii");
    }

    // Aargh! Multiple DataGeneratorEntrypoints under the namespace overwrite each other so I am forced to employ this method...
    @FunctionalInterface
    public interface Subsidiary extends Consumer<Consumer<FinishedRecipe>> { }

    protected static List<Subsidiary> EXPORTER_CONSUMERS = new ArrayList<>();

    public static void addSubsidiary(Subsidiary consumer)
    {
        EXPORTER_CONSUMERS.add(consumer);
    }

    static
    {
        System.out.println("MEAT RECOJIENF WINITIAITLISED");
    }
}
