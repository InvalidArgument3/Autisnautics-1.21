/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric call sites keep their shape. The body forwards to the NeoForge event.
 */


package net.fabricmc.fabric.api.client.model.loading.v1;

import com.neep.meatlib.event.FabricClientEvents;
import net.minecraft.resources.ResourceLocation;

public interface ModelLoadingPlugin {
    static void register(ModelLoadingPlugin plugin) {
        FabricClientEvents.modelPlugin(plugin);
    }

    void onInitializeModelLoader(Context context);

    @FunctionalInterface
    interface Context {
        void addModels(ResourceLocation... ids);
    }
}
