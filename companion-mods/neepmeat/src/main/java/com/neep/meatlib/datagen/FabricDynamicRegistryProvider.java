/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.datagen;

import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public abstract class FabricDynamicRegistryProvider {
    public FabricDynamicRegistryProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {}

    public static class Entries {
        public <T> void add(net.minecraft.resources.ResourceKey<T> key, T value) {}
    }
}
