/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.transfer;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record ItemVariant(Item item) {
    public static final ItemVariant BLANK = new ItemVariant(Items.AIR);

    public static ItemVariant of(Item item) {
        return item == null || item == Items.AIR ? BLANK : new ItemVariant(item);
    }

    public static ItemVariant of(ItemStack stack) {
        return stack.isEmpty() ? BLANK : new ItemVariant(stack.getItem());
    }

    public boolean isBlank() {
        return item == null || item == Items.AIR;
    }

    public Item getItem() {
        return item;
    }

    public ItemStack toStack(int count) {
        return isBlank() ? ItemStack.EMPTY : new ItemStack(item, count);
    }
}
