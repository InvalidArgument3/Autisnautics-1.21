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

package com.neep.neepmeat.machine.fabricator;

import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeMatching
{
    public static class Residual
    {
        public final Ingredient ingredient;
        public long residual;

        public Residual(Ingredient ingredient, int batchSize)
        {
            this.ingredient = ingredient;
            this.residual = batchSize;
        }
    }

    static Collection<Residual> findMatchingWithResiduals(int batchSize, List<Storage<ItemVariant>> inputs, List<Ingredient> ingredients, TransactionContext transaction) throws FabricatorLoopException
    {
        // Two inclusive cases for residual required ingredients:
        // 1. Some ingredients did not match
        // 2. Some ingredients matched, but amount was less than batch size

        // Collapse the ingredients.
        // In a recursive craft, multiple ingredients that end up matching the same item can cause multiple upstream crafts.
        // Collapsing ingredients that are equal should solve this in most cases.
        // It's unlikely that a recipe will use both minecraft:oak_planks and #minecraft:planks.
        Map<Ingredient, Residual> residuals1 = new HashMap<>();
        ingredients.forEach(i ->
        {
            if (!i.isEmpty())
            {
                var residual = residuals1.computeIfAbsent(i, i1 -> new Residual(i, 0));
                residual.residual += batchSize;
            }
        });

        Collection<Residual> residuals = residuals1.values();

        for (Storage<ItemVariant> storage : inputs)
        {
            for (StorageView<ItemVariant> view : storage.nonEmptyViews())
            {
                // Recipe has been matched fully
                if (residuals.isEmpty())
                {
                    return residuals;
                }

                // Ignore empty views
                if (view.isResourceBlank() || view.getAmount() <= 0)
                    continue;

                ItemVariant resource = view.getResource();

                var it = residuals.iterator();
                while (it.hasNext())
                {
                    Residual residual = it.next();

                    if (residual.ingredient.isEmpty())
                    {
                        it.remove();
                        continue;
                    }

                    ItemVariant extractedVariant = view.getResource();
                    if (residual.ingredient.test(extractedVariant.toStack()))
                    {
                        // Explicitly prevent loops and chains
                        if (view instanceof FabricatorBlockEntity.FabricatorStorage fabricatorStorage)
                            throw new FabricatorLoopException();

                        long extracted = view.extract(resource, residual.residual, transaction);
                        if (extracted == residual.residual)
                        {
                            ItemStack remainder = extractedVariant.getItem().getRecipeRemainder(extractedVariant.toStack(batchSize));

                            if (extractedVariant.getItem().getCraftingRemainingItem() != null)
                                storage.insert(ItemVariant.of(remainder), remainder.getCount(), transaction);

                            // Remove the ingredient once it is satisfied
                            it.remove();
                        }
                        else
                        {
                            // Case 1
                            residual.residual -= extracted;
                        }
                    }
                }
            }
        }

        // Case 2 implicitly handled
        return residuals;
    }

    // There is a problem here. If batchSize > 1 and all ingredients are present, but the input stacks are dispersed such that no individual stacks > batchSize, this will fail.
    // Probably fine when batchSize == 1
    static boolean findMatching(List<Storage<ItemVariant>> inputs, List<Ingredient> ingredients, List<ItemVariant> takenResources, TransactionContext transaction) throws FabricatorLoopException
    {
        for (Storage<ItemVariant> storage : inputs)
        {
            for (StorageView<ItemVariant> view : storage.nonEmptyViews())
            {
                // Recipe has been matched fully
                if (ingredients.isEmpty())
                    return true;

                // Ignore empty views
                if (view.isResourceBlank() || view.getAmount() <= 0)
                    continue;

                ItemVariant resource = view.getResource();

                var it = ingredients.iterator();
                while (it.hasNext())
                {
                    Ingredient ingredient = it.next();

                    if (ingredient.isEmpty())
                    {
                        it.remove();
                        continue;
                    }

                    ItemVariant extractedVariant = view.getResource();
                    if (ingredient.test(extractedVariant.toStack()))
                    {
                        // Explicitly prevent loops and chains
                        if (view instanceof FabricatorBlockEntity.FabricatorStorage fabricatorStorage)
                            throw new FabricatorLoopException();

                        // maxAmount = batchSize because crafting recipes only take 1 item for each ingredient.
                        long extracted = view.extract(resource, 1, transaction);
                        if (extracted == 1)
                        {
                            takenResources.add(extractedVariant);
                            ItemStack remainder = extractedVariant.getItem().getRecipeRemainder(extractedVariant.toStack(1));

                            if (extractedVariant.getItem().getCraftingRemainingItem() != null)
                                storage.insert(ItemVariant.of(remainder), remainder.getCount(), transaction);

                            // Remove the ingredient once it is satisfied
                            it.remove();
                        }

                    }
                }
            }
        }
        return ingredients.isEmpty();
    }

    static class FabricatorLoopException extends Exception
    {

    }
}
