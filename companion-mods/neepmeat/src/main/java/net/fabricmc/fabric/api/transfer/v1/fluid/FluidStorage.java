/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */


package net.fabricmc.fabric.api.transfer.v1.fluid;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public final class FluidStorage {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final BlockApiLookup<Storage<FluidVariant>, Direction> SIDED =
            BlockApiLookup.get(ResourceLocation.fromNamespaceAndPath("neepmeat", "fluid_storage"), (Class) Storage.class, Direction.class);
    private FluidStorage() {}
}
