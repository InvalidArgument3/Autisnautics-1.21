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

import com.neep.neepmeat.api.processing.BlockCrushingRegistry;
import com.neep.neepmeat.compat.rei.REIIngredientHelper;
import com.neep.neepmeat.recipe.BlockCrushingRecipe;
import com.neep.neepmeat.recipe.CrushingRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BlockCrushingDisplay implements Display
{
    private final CategoryIdentifier<? extends BlockCrushingDisplay> category;
    private final BlockCrushingRecipe recipe;
    private final Supplier<Collection<BlockCrushingRegistry.Entry>> entrySupplier;

    public BlockCrushingDisplay(CategoryIdentifier<? extends BlockCrushingDisplay> category, BlockCrushingRecipe recipe, Supplier<Collection<BlockCrushingRegistry.Entry>> entrySupplier)
    {
        this.category = category;
        this.recipe = recipe;
        this.entrySupplier = entrySupplier;
    }

    public static <T extends BlockCrushingRecipe> Function<T, BlockCrushingDisplay> filler(CategoryIdentifier<? extends BlockCrushingDisplay> categoryIdentifier, Supplier<Collection<BlockCrushingRegistry.Entry>> entrySupplier)
    {
        return r -> new BlockCrushingDisplay(categoryIdentifier, r, entrySupplier);
    }

    @Override
    public List<EntryIngredient> getInputEntries()
    {
        return List.of(
                EntryIngredients.ofItems(
                        entrySupplier.get()
                                .stream()
                                .flatMap(e -> e.input().getAll().stream())
                                .collect(Collectors.toUnmodifiableList()),
                        1)
        );
    }

    @Override
    public List<EntryIngredient> getOutputEntries()
    {
        EntryIngredient main = EntryIngredient.of(entrySupplier.get().stream().map(entry -> REIIngredientHelper.outputToStack(entry.output())).toList());
        EntryIngredient extra = EntryIngredient.of(entrySupplier.get().stream().map(entry -> REIIngredientHelper.outputToStack(entry.extra())
                .tooltip(Component.nullToEmpty("Chance: " + recipe.getChance()))).toList());
        return List.of(main, extra);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier()
    {
        return category;
    }

    public double processTime()
    {
        return recipe.getTime();
    }
}
