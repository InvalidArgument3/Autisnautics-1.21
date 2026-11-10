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
import com.neep.neepmeat.machine.mixer.MixingRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.ResourceLocation;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MixingDisplay extends BasicDisplay
{
    private MixingRecipe recipe;
    public int processTime;

    public MixingDisplay(MixingRecipe recipe)
    {
        this(
                List.of(
                        REIIngredientHelper.entryFromInput(recipe.getFluidInputs().get(0)),
                        REIIngredientHelper.entryFromInput(recipe.getFluidInputs().get(1)),
                        REIIngredientHelper.entryFromInput(recipe.getItemIngredient())
                        ),
                Collections.singletonList(EntryIngredients.of(recipe.getFluidOutput().resource(), recipe.getFluidOutput().minAmount())),
                Optional.empty()
        );
        this.recipe = recipe;
        this.processTime = recipe.getProcessTime();
    }

    public MixingDisplay(List<EntryIngredient> input, List<EntryIngredient> output, Optional<ResourceLocation> location)
    {
        super(input, output);
    }

    public MixingRecipe getRecipe()
    {
        return recipe;
    }

    public static Serializer<MixingDisplay> serializer()
    {
        return Serializer.ofSimple(MixingDisplay::new);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier()
    {
        return NMREIPlugin.MIXING;
    }

    public int getProcessTime()
    {
        return processTime;
    }
}
