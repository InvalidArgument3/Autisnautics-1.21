/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record ManufactureInput(ItemStack stack) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return stack;
    }

    @Override
    public int size() {
        return 1;
    }
}
