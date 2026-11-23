/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric call sites keep their shape. The body forwards to the NeoForge event.
 */


package net.fabricmc.fabric.api.client.rendering.v1;

import com.neep.meatlib.event.FabricClientEvents;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import java.util.function.Supplier;

public final class EntityModelLayerRegistry {
    private EntityModelLayerRegistry() {}

    public static void registerModelLayer(ModelLayerLocation location, Supplier<LayerDefinition> supplier) {
        FabricClientEvents.layer(location, supplier);
    }
}
