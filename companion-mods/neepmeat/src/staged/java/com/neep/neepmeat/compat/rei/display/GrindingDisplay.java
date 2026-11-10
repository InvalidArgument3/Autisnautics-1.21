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

import com.neep.neepmeat.compat.rei.NMCommonPlugin;
import com.neep.neepmeat.recipe.BlockCrushingRecipe;
import com.neep.neepmeat.recipe.CrushingRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class GrindingDisplay extends BasicDisplay
{
    private final CategoryIdentifier<?> categoryIdentifier;
    private CrushingRecipe recipe;

    public static boolean isValid(CrushingRecipe recipe)
    {
        return !recipe.destroy() && !(recipe instanceof BlockCrushingRecipe);
    }

    public GrindingDisplay(CategoryIdentifier<?> categoryIdentifier, CrushingRecipe recipe)
    {
        // CURSED
        this(categoryIdentifier,
                Util.make(() ->
                {
                    List<EntryIngredient> list = new ArrayList<>();
                    list.add(NMCommonPlugin.inputToIngredient(recipe.getItemInput()));
                    return list;
                }),
                Util.make(() ->
                {
                    List<EntryIngredient> list = new ArrayList<>();
                    list.add(EntryIngredients.ofItems(List.of(recipe.getItemOutput().resource()), (int) recipe.getItemOutput().minAmount()));

                    list.get(0).forEach(stack -> stack.tooltip(List.of(
                            Component.nullToEmpty("Min: " + recipe.getItemOutput().minAmount() + ", Max: " + recipe.getItemOutput().maxAmount()))));

                    if (recipe.getAuxOutput() != null)
                    {
                        list.add(EntryIngredients.ofItems(List.of(recipe.getAuxOutput().resource()), (int) recipe.getAuxOutput().minAmount()));
                        list.get(1).forEach(stack -> stack.tooltip(List.of(
                                Component.nullToEmpty("Chance: " + recipe.getAuxOutput().chance())
                        )));
                    }
                    return list;
                }),
                Optional.empty()
        );

        this.recipe = recipe;
    }

    public GrindingDisplay(CategoryIdentifier<?> categoryIdentifier, List<EntryIngredient> input, List<EntryIngredient> output, Optional<ResourceLocation> location)
    {
        super(input, output);
        this.categoryIdentifier = categoryIdentifier;
    }

    public static BasicDisplay.Serializer<GrindingDisplay> serializer(CategoryIdentifier<?> categoryIdentifier)
    {
        return BasicDisplay.Serializer.ofSimple((input, output, location1) -> new GrindingDisplay(categoryIdentifier, input, output, location1));
    }

    public static <T extends CrushingRecipe> Function<T, GrindingDisplay> filler(CategoryIdentifier<? extends GrindingDisplay> categoryIdentifier)
    {
        return r -> new GrindingDisplay(categoryIdentifier, r);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier()
    {
        return categoryIdentifier;
    }

    public double processTime()
    {
        return recipe.getTime();
    }
}
