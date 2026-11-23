/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.compat.emi;

import com.neep.neepmeat.recipe.AlloyKilnRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AlloyEmiRecipe implements EmiRecipe {
    private final RecipeHolder<AlloyKilnRecipe> holder;

    public AlloyEmiRecipe(RecipeHolder<AlloyKilnRecipe> holder) {
        this.holder = holder;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return NeepEmiClient.ALLOY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return holder.id();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        AlloyKilnRecipe recipe = holder.value();
        return List.of(EmiStack.of(recipe.input1().stack()), EmiStack.of(recipe.input2().stack()));
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(EmiStack.of(holder.value().output().stack()));
    }

    @Override
    public int getDisplayWidth() {
        return 100;
    }

    @Override
    public int getDisplayHeight() {
        return 24;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(getInputs().get(0), 0, 0);
        widgets.addSlot(getInputs().get(1), 20, 0);
        widgets.addSlot(getOutputs().get(0), 70, 0).recipeContext(this);
    }
}
