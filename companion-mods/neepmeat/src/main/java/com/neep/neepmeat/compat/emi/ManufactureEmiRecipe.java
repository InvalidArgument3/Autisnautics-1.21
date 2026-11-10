/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.compat.emi;

import com.neep.neepmeat.recipe.ManufactureRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ManufactureEmiRecipe implements EmiRecipe {
    private final RecipeHolder<ManufactureRecipe> holder;

    public ManufactureEmiRecipe(RecipeHolder<ManufactureRecipe> holder) {
        this.holder = holder;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return NeepEmiClient.MANUFACTURE;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return holder.id();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        ManufactureRecipe recipe = holder.value();
        List<EmiIngredient> inputs = new ArrayList<>();
        inputs.add(EmiStack.of(recipe.baseStack()));
        for (ManufactureRecipe.ManufactureStepSpec step : recipe.steps()) {
            ItemStack icon = step.icon();
            if (!icon.isEmpty()) {
                inputs.add(EmiStack.of(icon));
            }
        }
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(EmiStack.of(holder.value().resultStack()));
    }

    @Override
    public int getDisplayWidth() {
        return 140;
    }

    @Override
    public int getDisplayHeight() {
        return 24;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int x = 0;
        for (EmiIngredient input : getInputs()) {
            widgets.addSlot(input, x, 0);
            x += 18;
        }
        widgets.addSlot(getOutputs().get(0), 110, 0).recipeContext(this);
    }
}
