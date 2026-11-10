/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.ArrayList;
import java.util.List;
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

    public static final List<Object> GATHERED = new ArrayList<>();

    public static void onGatherData(GatherDataEvent event) {
        FabricDataGenerator generator = new FabricDataGenerator(event.getGenerator().getPackOutput(), event.getLookupProvider());
        for (String name : List.of("com.neep.neepmeat.datagen.NMDataGen")) {
            try {
                Class<?> type = Class.forName(name);
                Object entry = type.getDeclaredConstructor().newInstance();
                type.getMethod("onInitializeDataGenerator", FabricDataGenerator.class).invoke(entry, generator);
            } catch (ReflectiveOperationException ignored) {
                // The entry point stays excluded until its neighbours compile.
            }
        }
        for (Object provider : GATHERED) {
            if (provider instanceof DataProvider data) {
                event.getGenerator().addProvider(event.includeServer(), data);
            }
        }
    }

    public static class Pack {
        private final PackOutput output;
        private final CompletableFuture<HolderLookup.Provider> registries;

        public Pack(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            this.output = output;
            this.registries = registries;
        }

        private FabricDataOutput dataOutput() {
            if (output instanceof FabricDataOutput existing) {
                return existing;
            }
            return new FabricDataOutput(output.getOutputFolder());
        }

        public <T> void addProvider(Factory<T> factory) {
            GATHERED.add(factory.create(dataOutput()));
        }

        public <T> void addProvider(RegistryDependentFactory<T> factory) {
            GATHERED.add(factory.create(dataOutput(), registries));
        }

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
