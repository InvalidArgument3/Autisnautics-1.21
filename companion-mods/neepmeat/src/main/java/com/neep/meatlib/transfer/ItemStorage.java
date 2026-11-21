/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.transfer;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class ItemStorage {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final BlockApiLookup<Storage<ItemVariant>, Direction> SIDED =
            BlockApiLookup.get(ResourceLocation.fromNamespaceAndPath("neepmeat", "item_storage"), (Class) Storage.class, Direction.class);

    private ItemStorage() {}

    public static ItemVariant of(ItemStack stack) {
        return ItemVariant.of(stack);
    }
}
