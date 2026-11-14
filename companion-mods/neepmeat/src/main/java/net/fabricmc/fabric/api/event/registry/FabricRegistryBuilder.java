/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Custom registries are vanilla MappedRegistry values. They are not in the registry root.
 */

package net.fabricmc.fabric.api.event.registry;

import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class FabricRegistryBuilder<T, R extends Registry<T>> {
    private final ResourceKey<Registry<T>> key;
    private final ResourceLocation defaultId;

    private FabricRegistryBuilder(ResourceKey<Registry<T>> key, ResourceLocation defaultId) {
        this.key = key;
        this.defaultId = defaultId;
    }

    public static <T> FabricRegistryBuilder<T, MappedRegistry<T>> createSimple(ResourceKey<Registry<T>> key) {
        return new FabricRegistryBuilder<>(key, null);
    }

    public static <T> FabricRegistryBuilder<T, MappedRegistry<T>> createSimple(Class<T> type, ResourceLocation id) {
        return createSimple(ResourceKey.createRegistryKey(id));
    }

    public static <T> FabricRegistryBuilder<T, DefaultedMappedRegistry<T>> createDefaulted(ResourceKey<Registry<T>> key, ResourceLocation defaultId) {
        return new FabricRegistryBuilder<>(key, defaultId);
    }

    @SuppressWarnings("unchecked")
    public R buildAndRegister() {
        if (defaultId != null) {
            return (R) new DefaultedMappedRegistry<>(defaultId.toString(), key, com.mojang.serialization.Lifecycle.stable(), false);
        }
        return (R) new MappedRegistry<>(key, com.mojang.serialization.Lifecycle.stable());
    }
}
