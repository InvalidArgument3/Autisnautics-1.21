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

package com.neep.meatlib.recipe;

import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

@SuppressWarnings("UnstableApiUsage")
public interface MeatlibRecipe<C> extends Recipe<ImplementedRecipe.DummyInventory>, RecipeContextMatch
{
    boolean matches(C context);

    @Override
    @SuppressWarnings("unchecked")
    default boolean matches(Object input, Level level) {
        if (input == null) {
            return false;
        }
        try {
            return matches((C) input);
        } catch (ClassCastException ignored) {
            return false;
        }
    }

//    RecipeSerializer<?> getSerialiser();

    boolean takeInputs(C context, TransactionContext transaction);

    boolean ejectOutputs(C context, TransactionContext transaction);

//    MeatRecipeType<?> getType();

    MeatRecipeSerialiser<?> getSerializer();

    ResourceLocation getId();

    @Override
    default ItemStack getToastSymbol()
    {
        return ItemStack.EMPTY;
    }

    @Override
    default String getGroup()
    {
        return "neepmeat";
    }

    @Override
    default NonNullList<Ingredient> getIngredients()
    {
        return NonNullList.create();
    }

    @Override
    default boolean matches(ImplementedRecipe.DummyInventory inventory, Level world)
    {
        return false;
    }

    default ItemStack craft(ImplementedRecipe.DummyInventory inventory, RegistryAccess dynamicRegistryManager)
    {
        return ItemStack.EMPTY;
    }

    @Override
    default boolean canCraftInDimensions(int width, int height)
    {
        return true;
    }

    default NonNullList<ItemStack> getRemainder(ImplementedRecipe.DummyInventory inventory)
    {
        return NonNullList.create();
    }

    default ItemStack getResultItem(RegistryAccess registryManager)
    {
        return ItemStack.EMPTY;
    }

    @Override
    default boolean isSpecial()
    {
        return true;
    }

    @Override
    default boolean isIncomplete()
    {
        return false;
    }
}
