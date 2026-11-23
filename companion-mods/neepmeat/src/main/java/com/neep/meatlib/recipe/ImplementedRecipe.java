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

import com.neep.meatlib.inventory.ImplementedInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

/**
 * This class overrides some Vanilla recipe methods so that they do not have to be overridden in every subclass.
 */
public abstract class ImplementedRecipe<T extends ImplementedRecipe.DummyInventory> implements Recipe<T>
{
    @Override
    public ItemStack assemble(T inventory, HolderLookup.Provider registryManager)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider dynamicRegistryManager)
    {
        return ItemStack.EMPTY;
    }

    public interface DummyInventory extends ImplementedInventory
    {
        default NonNullList<ItemStack> getItems()
        {
            return NonNullList.create();
        }
    }
}
