/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.transfer;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public final class FluidStorage {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup<Storage<FluidVariant>, com.neep.meatlib.transfer.ContainerItemContext> ITEM =
            net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup.get(ResourceLocation.fromNamespaceAndPath("neepmeat", "fluid_item"), storageClass(), com.neep.meatlib.transfer.ContainerItemContext.class);

    @SuppressWarnings("unchecked")
    private static Class<Storage<FluidVariant>> storageClass() {
        return (Class) Storage.class;
    }

    public static final BlockApiLookup<Storage<FluidVariant>, Direction> SIDED =
            BlockApiLookup.get(ResourceLocation.fromNamespaceAndPath("neepmeat", "fluid_storage"), (Class) Storage.class, Direction.class);

    private FluidStorage() {}
}
