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

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.api.processing.ball_mill.BallMillRecipe;
import com.neep.neepmeat.compat.emi.NMEmiPlugin;
import com.neep.neepmeat.compat.emi.helper.EmiIngredientHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class BallMillEmiRecipe implements EmiRecipe
{
    private final EmiIngredient input;
    private final List<EmiStack> outputs;
    private final float processTime;
    private final ResourceLocation id;

    public BallMillEmiRecipe(BallMillRecipe recipe)
    {
        this.id = recipe.getId();
        this.processTime = recipe.getTime();
        this.input = EmiIngredientHelper.inputToIngredient(recipe.getInput());
//        this.outputs = List.of(EmiStack.of(recipe.getOutputs().resource(), recipe.getOutputs().minAmount()));
        this.outputs = recipe.getOutputs().stream().map(EmiIngredientHelper::outputToStack).toList();
    }

    @Override
    public EmiRecipeCategory getCategory()
    {
        return NMEmiPlugin.BALL_MILL;
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
        return outputs;
    }

    @Override
    public int getDisplayWidth()
    {
        return 150;
    }

    @Override
    public int getDisplayHeight()
    {
        return Math.max(55, 20 + (outputs.size() / 3 * 18));
    }

    @Override
    public void addWidgets(WidgetHolder widgets)
    {
        int ny = Mth.ceil(outputs.size() / 3f);
        int startX = getDisplayWidth() / 2 - 60;
        int startY = (getDisplayHeight() - 18) / 2;

        widgets.addText(NeepMeat.translationKey("category", "general.energy", PowerUtil.energyForRecipe(processTime)), getDisplayWidth() - 5, 3, 0xFF404040, false).horizontalAlign(TextWidget.Alignment.END);

        widgets.addSlot(input, startX, startY);
        widgets.addFillingArrow(startX + 18 + 5, getDisplayHeight() / 2 - 8, (int) (processTime * 1000 / 20));

        int outputX = startX + 51;
        int outputY = (getDisplayHeight() - ny * 18) / 2;
        for (int j = 0; j < ny; ++j)
        {
            for (int i = 0; i < 3; ++i)
            {
                int idx = j * 3 + i;
                if (idx < outputs.size())
                {
                    EmiIngredient ingredient = outputs.get(idx);
                    widgets.addSlot(ingredient, outputX + i * 18, outputY + j * 18).recipeContext(this);
                }
                else
                {
                    widgets.addSlot(outputX + i * 18, outputY + j * 18).recipeContext(this);
                }
            }
        }
    }
}
