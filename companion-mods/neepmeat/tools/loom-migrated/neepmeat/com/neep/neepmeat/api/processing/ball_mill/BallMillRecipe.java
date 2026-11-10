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

package com.neep.neepmeat.api.processing.ball_mill;

import com.neep.meatlib.recipe.MeatlibRecipe;
import com.neep.meatlib.recipe.ingredient.RecipeOutput;
import com.neep.meatlib.recipe.ingredient.RegistryRecipeInput;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.world.item.Item;
import java.util.List;

public interface BallMillRecipe extends MeatlibRecipe<BallMillRecipe.BallMillContext>
{
    int getTime();

    RegistryRecipeInput<Item> getInput();
    List<RecipeOutput<Item>> getOutputs();

    interface BallMillContext
    {
        StorageView<ItemVariant> getInput();

        Storage<ItemVariant> getOutput();

        record Match(StorageView<ItemVariant> input) implements BallMillContext
        {
            @Override
            public StorageView<ItemVariant> getInput()
            {
                return input;
            }

            @Override
            public Storage<ItemVariant> getOutput()
            {
                return Storage.empty();
            }
        }

        record Process(StorageView<ItemVariant> input, Storage<ItemVariant> output) implements BallMillContext
        {
            @Override
            public StorageView<ItemVariant> getInput()
            {
                return input;
            }

            @Override
            public Storage<ItemVariant> getOutput()
            {
                return output;
            }
        }
    }
}
