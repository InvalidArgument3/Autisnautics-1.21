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
import com.neep.neepmeat.compat.emi.NMEmiPlugin;
import com.neep.neepmeat.compat.emi.helper.EmiIngredientHelper;
import com.neep.neepmeat.recipe.BioreactorRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class BioreactorEmiRecipe implements EmiRecipe
{
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;
    private final float processTime;
    private final ResourceLocation id;

    public BioreactorEmiRecipe(net.minecraft.world.item.crafting.RecipeHolder<BioreactorRecipe> holder)
    {
        BioreactorRecipe recipe = holder.value();
        this.id = holder.id();
        this.processTime = recipe.getProcessTime();
        this.inputs = Stream.concat(recipe.getItemInputs().stream(), recipe.getFluidInputs().stream())
                .map(EmiIngredientHelper::inputToIngredient)
                .toList();
        this.outputs = List.of(EmiStack.of(recipe.getOutputs().resource(), recipe.getOutputs().minAmount()));
    }

    @Override
    public EmiRecipeCategory getCategory()
    {
        // TODO
        return null;
//        return NMEmiPlugin.BIOREACTOR;
    }

    @Override
    public @Nullable ResourceLocation getId()
    {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs()
    {
        return inputs;
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
        return Math.max(55, 20 + (inputs.size() / 3 * 18));
    }

    @Override
    public void addWidgets(WidgetHolder widgets)
    {
        int ny = Mth.ceil(inputs.size() / 3f);
        int startX = getDisplayWidth() / 2 - 60;
        int startY = (getDisplayHeight() - ny * 18) / 2 + 5;
        int outputY = (getDisplayHeight() - 18) / 2;
        for (int j = 0; j < ny; ++j)
        {
            for (int i = 0; i < 3; ++i)
            {
                int idx = j * 3 + i;
                if (idx < inputs.size())
                {
                    EmiIngredient ingredient = inputs.get(idx);
                    widgets.addSlot(ingredient, startX + i * 18, startY + j * 18);
                }
                else
                {
                    widgets.addSlot(startX + i * 18, startY + j * 18);
                }
            }
        }

        DecimalFormat df = new DecimalFormat("###.##");
        widgets.addText(Component.translatable("category." + NeepMeat.NAMESPACE + ".mixing.time", df.format(processTime / 20d)), getDisplayWidth() - 5, 5, 0xFF404040, false).horizontalAlign(TextWidget.Alignment.END);
        widgets.addFillingArrow(startX + 3 * 18 + 5, getDisplayHeight() / 2 - 8, (int) (processTime * 1000 / 20));

        widgets.addSlot(outputs.get(0), startX + 91, outputY).recipeContext(this);
    }
}
