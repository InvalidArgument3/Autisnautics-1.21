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
import com.neep.neepmeat.recipe.PressingRecipe;
import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PressingDisplay extends BasicDisplay
{
    private PressingRecipe recipe;

    public PressingDisplay(PressingRecipe recipe)
    {
        this(
                List.of(EntryIngredients.of(VanillaEntryTypes.FLUID, recipe.getFluidInput().getAll()
                        .stream()
                        .map(f -> FluidStack.create(f, recipe.getFluidInput().amount()))
                        .collect(Collectors.toList()))),
                new ArrayList<>(List.of(
                        EntryIngredients.ofItems(List.of(recipe.getItemOutput().resource()), (int) recipe.getItemOutput().minAmount())
                )),
                Optional.empty()
        );
        this.recipe = recipe;
    }

    public PressingDisplay(List<EntryIngredient> input, List<EntryIngredient> output, Optional<ResourceLocation> location)
    {
        super(input, output);
    }

    public static Serializer<PressingDisplay> serializer()
    {
        return Serializer.ofSimple(PressingDisplay::new);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier()
    {
        return NMREIPlugin.PRESSING;
    }
}
