/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.recipe;

import com.neep.neepmeat.NeepMeat;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeepComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, NeepMeat.ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WorkpieceSteps>> WORKPIECE =
            COMPONENTS.register("workpiece", () -> DataComponentType.<WorkpieceSteps>builder()
                    .persistent(WorkpieceSteps.CODEC)
                    .networkSynchronized(WorkpieceSteps.STREAM_CODEC)
                    .build());

    /** Upstream stored this as the item NBT key {@code ore_item}. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> ORE_ITEM =
            COMPONENTS.register("ore_item", () -> DataComponentType.<ResourceLocation>builder()
                    .persistent(ResourceLocation.CODEC)
                    .networkSynchronized(ResourceLocation.STREAM_CODEC)
                    .build());

    private NeepComponents() {}
}
