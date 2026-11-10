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

package com.neep.neepmeat.compat.rei;

import com.neep.meatlib.recipe.ingredient.IngredientType;
import com.neep.meatlib.recipe.ingredient.Ingredients;
import com.neep.meatlib.recipe.ingredient.RecipeOutput;
import com.neep.meatlib.recipe.ingredient.RegistryRecipeInput;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class REIIngredientHelper
{
    // AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAaa
    private static final Map<IngredientType<?>, Function<RegistryRecipeInput<?>, EntryIngredient>> ENTRY_STACK_MAP = new HashMap<>();

    static
    {
        REIIngredientHelper.ENTRY_STACK_MAP.put(Ingredients.ITEM, input -> fromItem((RegistryRecipeInput<ItemLike>) input));

        REIIngredientHelper.ENTRY_STACK_MAP.put(Ingredients.FLUID, input -> fromFluid((RegistryRecipeInput<Fluid>) input));
    }

    public static EntryIngredient fromItem(RegistryRecipeInput<ItemLike> input)
    {
        return EntryIngredients.ofItems(input.getAll(), (int) input.amount());
    }

    public static EntryIngredient fromFluid(RegistryRecipeInput<Fluid> input)
    {
        Collection<Fluid> fluids = input.getAll();
        EntryIngredient.Builder result = EntryIngredient.builder(fluids.size());
        for (Fluid fluid : fluids)
        {
            result.add(EntryStacks.of(fluid, input.amount()));
        }
        return result.build();
    }

    public static <T> EntryIngredient entryFromInput(RegistryRecipeInput<T> input)
    {
        if (input.isEmpty())
            return EntryIngredient.empty();

        Collection<T> matching = input.getAll();

        if (matching.isEmpty())
            return EntryIngredient.empty();

        return ENTRY_STACK_MAP.get(input.getType()).apply(input);
    }

    public static EntryStack<ItemStack> outputToStack(RecipeOutput<Item> output)
    {
        ItemStack stack = new ItemStack(output.resource(), (int) output.minAmount());
        stack.setTag(output.getNbt());
        return EntryStacks.of(stack);
    }
}
