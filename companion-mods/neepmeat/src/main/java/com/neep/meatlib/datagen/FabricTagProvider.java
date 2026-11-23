/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class FabricTagProvider {
    public abstract static class BlockTagProvider extends BlockTagsProvider {
        protected final ResourceKey<? extends Registry<Block>> registryKey = Registries.BLOCK;

        public BlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries, "neepmeat", null);
        }
    }

    public abstract static class ItemTagProvider extends IntrinsicHolderTagsProvider<Item> {
        protected final ResourceKey<? extends Registry<Item>> registryKey = Registries.ITEM;

        public ItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, Registries.ITEM, registries, item -> item.builtInRegistryHolder().key());
        }
    }
}
