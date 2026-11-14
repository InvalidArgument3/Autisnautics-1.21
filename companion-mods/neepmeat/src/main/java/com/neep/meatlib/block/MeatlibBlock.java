/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.block;

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public interface MeatlibBlock {
    default BlockItem blockItem() {
        return (BlockItem) net.minecraft.world.item.Item.BY_BLOCK.get(this);
    }

    default boolean autoGenDrop() {
        return true;
    }

    default net.minecraft.world.level.storage.loot.LootTable.Builder genLoot(Object provider) {
        return null;
    }

    default net.minecraft.world.level.ItemLike dropsLike() {
        return this instanceof net.minecraft.world.level.ItemLike item ? item : null;
    }

    static String structure(String path) {
        return path + "_structure";
    }

    @FunctionalInterface
    interface ItemFactory {
        BlockItem create(Block block, RegistrationContext context, ItemSettings settings);
    }
}
