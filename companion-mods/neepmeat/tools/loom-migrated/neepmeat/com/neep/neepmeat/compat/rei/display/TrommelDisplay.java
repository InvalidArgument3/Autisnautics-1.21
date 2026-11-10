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

package com.neep.neepmeat.compat.rei.display;

import com.neep.neepmeat.compat.rei.NMREIPlugin;
import com.neep.neepmeat.compat.rei.REIIngredientHelper;
import com.neep.neepmeat.recipe.NormalTrommelRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrommelDisplay extends BasicDisplay
{
    private NormalTrommelRecipe recipe;

    public TrommelDisplay(NormalTrommelRecipe recipe)
    {
        this(
                List.of(
                        REIIngredientHelper.entryFromInput(recipe.getFluidInput())
                        ),
                new ArrayList<>(),
                Optional.empty()
        );
        this.recipe = recipe;
        this.outputs.add(EntryIngredients.of(recipe.getFluidOutput().resource(), recipe.getFluidOutput().minAmount()));
        if (recipe.getAuxOutput() != null)
        {
            this.outputs.add(EntryIngredients.ofItems(List.of(recipe.getAuxOutput().resource()), (int) recipe.getAuxOutput().minAmount()));
            this.outputs.get(1).forEach(stack -> stack.tooltip(List.of(
                    Component.nullToEmpty("Chance: " + recipe.getAuxOutput().chance())
            )));
        }
    }

    public TrommelDisplay(List<EntryIngredient> input, List<EntryIngredient> output, Optional<ResourceLocation> location)
    {
        super(input, output);
    }

    public NormalTrommelRecipe getRecipe()
    {
        return recipe;
    }

    public static Serializer<TrommelDisplay> serializer()
    {
        return Serializer.ofSimple(TrommelDisplay::new);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier()
    {
        return NMREIPlugin.TROMMEL;
    }
}
