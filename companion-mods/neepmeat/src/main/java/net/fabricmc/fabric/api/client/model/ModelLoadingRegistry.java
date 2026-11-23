/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric call sites keep their shape. The body forwards to the NeoForge event.
 */


package net.fabricmc.fabric.api.client.model;

/** Older Fabric model hook. Extra models now go through {@link loading.v1.ModelLoadingPlugin}. */
public final class ModelLoadingRegistry {
    public static final ModelLoadingRegistry INSTANCE = new ModelLoadingRegistry();
    private ModelLoadingRegistry() {}
    public void registerModelProvider(ExtraModelProvider provider) {}
}
