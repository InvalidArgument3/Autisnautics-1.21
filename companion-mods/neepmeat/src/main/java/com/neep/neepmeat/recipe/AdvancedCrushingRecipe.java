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

package com.neep.neepmeat.recipe;

import com.neep.meatlib.recipe.MeatRecipeSerialiser;
import com.neep.meatlib.recipe.MeatRecipeType;
import com.neep.meatlib.recipe.ingredient.RegistryRecipeInput;
import com.neep.meatlib.recipe.ingredient.RecipeInputs;
import com.neep.meatlib.recipe.ingredient.RecipeOutput;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.grinder.CrusherRecipeContext;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class AdvancedCrushingRecipe extends CrushingRecipe
{
    public AdvancedCrushingRecipe(ResourceLocation id, RegistryRecipeInput<Item> itemInput, RecipeOutput<Item> itemOutput, RecipeOutput<Item> extraOutput, float experience, int processTime)
    {
        super(id, itemInput, itemOutput, extraOutput, experience, processTime);
    }

    @Override
    public MeatRecipeType<?> getType()
    {
        return NMrecipeTypes.ADVANCED_CRUSHING;
    }

    @Override
    public MeatRecipeSerialiser<?> getSerializer()
    {
        return NMrecipeTypes.ADVANCED_CRUSHING_SERIALIZER;
    }

    public static class DestroyRecipe extends AdvancedCrushingRecipe
    {
        public DestroyRecipe(ResourceLocation id)
        {
            super(id, RecipeInputs.empty(), RecipeOutput.empty(), RecipeOutput.empty(), 0, 40);
        }

        @Override
        public boolean destroy()
        {
            return true;
        }

        @Override
        public boolean matches(CrusherRecipeContext inventory)
        {
            return false;
        }

        @Override
        public boolean takeInputs(CrusherRecipeContext storage, TransactionContext transaction)
        {
            var it = storage.getInputStorage().nonEmptyIterator();
            if (it.hasNext())
            {
                StorageView<ItemVariant> view = it.next();
                view.extract(view.getResource(), 1, transaction);
                return true;
            }
            return false;
        }

        @Override
        public boolean ejectOutputs(CrusherRecipeContext context, TransactionContext transaction)
        {
            return true;
        }
    }
}
