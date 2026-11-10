/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.datagen;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.ItemLike;

/** Fabric recipe provider shape. Subclasses fill {@link #buildRecipes}. */
public abstract class FabricRecipeProvider {
    public FabricRecipeProvider(FabricDataOutput output) {}

    public void buildRecipes(RecipeOutput output) {}

    public String getName() {
        return "recipes";
    }

    public static Criterion<?> has(ItemLike item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(item);
    }

    public static void slab(RecipeOutput exporter, net.minecraft.data.recipes.RecipeCategory category, ItemLike output, ItemLike input) {}

    public static void wall(RecipeOutput exporter, net.minecraft.data.recipes.RecipeCategory category, ItemLike output, ItemLike input) {}

    public static String getHasName(ItemLike item) {
        return "has_" + item.asItem();
    }
}
