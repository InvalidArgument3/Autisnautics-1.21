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

    private NeepComponents() {}
}
