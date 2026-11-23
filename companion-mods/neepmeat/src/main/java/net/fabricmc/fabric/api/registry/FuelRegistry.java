/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Burn time comes from the vanilla fuel check.
 */

package net.fabricmc.fabric.api.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public final class FuelRegistry {
    public static final FuelRegistry INSTANCE = new FuelRegistry();

    private FuelRegistry() {}

    public Integer get(Item item) {
        if (item == null) {
            return null;
        }
        int time = new ItemStack(item).getBurnTime(RecipeType.SMELTING);
        return time > 0 ? time : null;
    }
}
