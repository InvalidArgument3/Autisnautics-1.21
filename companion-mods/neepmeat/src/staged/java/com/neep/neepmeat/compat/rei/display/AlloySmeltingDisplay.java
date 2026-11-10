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
import com.neep.neepmeat.recipe.AlloyKilnRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class AlloySmeltingDisplay extends BasicDisplay
{
    private AlloyKilnRecipe recipe;

    public AlloySmeltingDisplay(AlloyKilnRecipe recipe)
    {
        this(
                List.of(
                        EntryIngredients.ofItems((Collection<ItemLike>) (Object) recipe.getItemInput1().getAll(), (int) recipe.getItemInput1().amount()),
                        EntryIngredients.ofItems((Collection<ItemLike>) (Object) recipe.getItemInput2().getAll(), (int) recipe.getItemInput2().amount())
                ),
                new ArrayList<>(List.of(
                        EntryIngredients.ofItems(List.of(recipe.getItemOutput().resource()), (int) recipe.getItemOutput().minAmount())
                )),
                Optional.empty()
        );
        this.recipe = recipe;
        this.outputs.get(0).forEach(stack -> stack.tooltip(List.of(
                Component.nullToEmpty("Min: " + recipe.getItemOutput().minAmount() + ", Max: " + recipe.getItemOutput().maxAmount())
        )));
    }

    public AlloySmeltingDisplay(List<EntryIngredient> input, List<EntryIngredient> output, Optional<ResourceLocation> location)
    {
        super(input, output);
    }

    public static Serializer<AlloySmeltingDisplay> serializer()
    {
        return Serializer.ofSimple(AlloySmeltingDisplay::new);
    }

    public int getProcessTime()
    {
        return recipe.getProcessTime();
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier()
    {
        return NMREIPlugin.ALLOY_SMELTING;
    }

}
