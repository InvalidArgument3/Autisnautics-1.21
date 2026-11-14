/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.compat.jei;

import com.neep.neepmeat.recipe.AlloyKilnRecipe;
import com.neep.neepmeat.recipe.ManufactureRecipe;
import com.neep.neepmeat.recipe.NeepRecipes;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public final class NeepJeiClient {
    private NeepJeiClient() {}

    public static void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new AlloyKilnCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ManufactureCategory(registration.getJeiHelpers().getGuiHelper()));
        for (String id : com.neep.neepmeat.compat.emi.NeepEmiClient.DROPPED_REI) {
            registration.addRecipeCategories(new DroppedReiCategory(registration.getJeiHelpers().getGuiHelper(), id));
        }
    }

    public static void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(AlloyKilnCategory.TYPE, current());
        registration.addRecipes(ManufactureCategory.TYPE, manufacture());
    }

    private static List<RecipeHolder<AlloyKilnRecipe>> current() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            return minecraft.level.getRecipeManager().getAllRecipesFor(NeepRecipes.ALLOY.get());
        }
        if (minecraft.getConnection() != null) {
            return minecraft.getConnection().getRecipeManager().getAllRecipesFor(NeepRecipes.ALLOY.get());
        }
        return List.of();
    }

    private static List<RecipeHolder<ManufactureRecipe>> manufacture() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            return minecraft.level.getRecipeManager().getAllRecipesFor(NeepRecipes.MANUFACTURE.get());
        }
        if (minecraft.getConnection() != null) {
            return minecraft.getConnection().getRecipeManager().getAllRecipesFor(NeepRecipes.MANUFACTURE.get());
        }
        return List.of();
    }
}
