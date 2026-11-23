/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Conventional `c:` tags. Fabric's convention tag classes are not a dependency.
 */

package com.neep.meatlib.datagen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ConventionalTags {
    private ConventionalTags() {}

    public static final class ConventionalBlockTags {
        public static final TagKey<Block> ORES = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "ores"));

        private ConventionalBlockTags() {}
    }

    public static final class ConventionalItemTags {
        public static final TagKey<Item> ORES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ores"));
        public static final TagKey<Item> RAW_ORES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "raw_ores"));

        private ConventionalItemTags() {}
    }
}
