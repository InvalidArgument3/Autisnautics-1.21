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

package com.neep.neepmeat.compat.emi.recipe;

import com.neep.meatlib.recipe.ingredient.RecipeOutput;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.processing.BlockCrushingRegistry;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.compat.emi.helper.EmiIngredientHelper;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.recipe.BlockCrushingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class BlockCrushingEmiRecipe implements EmiRecipe
{
    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final EmiIngredient input;
    private final RecipeOutput<?> recipeOutput;
    private final EmiStack output;
    private final EmiStack extraOutput;
    private final int processTime;

    public BlockCrushingEmiRecipe(int index, EmiRecipeCategory category, BlockCrushingRecipe recipe, BlockCrushingRegistry.Entry entry)
    {
        this.category = category;
        this.id = new ResourceLocation(recipe.getId().getNamespace(), "/" + recipe.getId().getPath() + "_" + index);
        this.processTime = recipe.getTime();
        this.input = EmiIngredientHelper.inputToIngredient(entry.input());
        this.recipeOutput = entry.output();
        this.output = EmiIngredientHelper.outputToStack(entry.output());
        this.extraOutput = EmiIngredientHelper.outputToStack(entry.extra()).setChance(entry.extra().chance());

    }

    @Override
    public EmiRecipeCategory getCategory()
    {
        return category;
    }

    @Override
    public @Nullable ResourceLocation getId()
    {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs()
    {
        return List.of(input);
    }

    @Override
    public List<EmiStack> getOutputs()
    {
        return List.of(output, extraOutput);
    }

    @Override
    public int getDisplayWidth()
    {
        return 150;
    }

    @Override
    public int getDisplayHeight()
    {
        return 55;
    }

    @Override
    public void addWidgets(WidgetHolder widgets)
    {
        int startX = getDisplayWidth() / 2 - 41;
        int startY = 10;

        widgets.addText(NeepMeat.translationKey("category", "general.energy", PowerUtil.energyForRecipe(processTime)), getDisplayWidth() - 5, 3, 0xFF404040, false).horizontalAlign(TextWidget.Alignment.END);

        widgets.addTexture(EmiTexture.EMPTY_ARROW, startX + 25, startY + 9);

        widgets.addSlot(getInputs().get(0), startX + 1, startY + 9);

        if (!output.isEmpty())
        {
            long amount = output.getAmount();

            widgets.addSlot(EmiIngredient.of(List.of(output), amount), startX + 61, startY + 9)
                    .recipeContext(this)
                    .appendTooltip(NMrecipeTypes.ofMinMax(recipeOutput.minAmount(), recipeOutput.maxAmount()))
            ;

            widgets.addSlot(EmiIngredient.of(List.of(extraOutput)), startX + 81, startY + 9).recipeContext(this);
        }
    }
}
