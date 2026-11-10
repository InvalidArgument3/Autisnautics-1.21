/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

/** Fabric pack registration. NeoForge gathers the same providers from {@code GatherDataEvent}. */
public class FabricDataGenerator {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public FabricDataGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.output = output;
        this.registries = registries;
    }

    public Pack createPack() {
        return new Pack(output, registries);
    }

    public static class Pack {
        public Pack(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {}

        public <T> void addProvider(Factory<T> factory) {}

        public <T> void addProvider(RegistryDependentFactory<T> factory) {}

        @FunctionalInterface
        public interface Factory<T> {
            T create(FabricDataOutput output);
        }

        @FunctionalInterface
        public interface RegistryDependentFactory<T> {
            T create(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries);
        }
    }
}
