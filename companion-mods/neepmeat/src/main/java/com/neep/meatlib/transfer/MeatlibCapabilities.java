/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.transfer;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.function.BiFunction;

/**
 * Fabric {@code FluidStorage} / {@code ItemStorage} lookups become NeoForge block capabilities.
 * Machines register their block entities through these helpers. Nothing is registered until a
 * machine exists, but the event is subscribed so the hook is live.
 */
public final class MeatlibCapabilities {
    private MeatlibCapabilities() {}

    public static void register(RegisterCapabilitiesEvent event) {
        // Presence of the event is the boot check. Machine block entities attach below.
    }

    public static <T extends BlockEntity> void itemHandler(RegisterCapabilitiesEvent event, BlockEntityType<T> type,
                                                           BiFunction<T, net.minecraft.core.Direction, IItemHandler> getter) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, getter::apply);
    }

    public static <T extends BlockEntity> void fluidHandler(RegisterCapabilitiesEvent event, BlockEntityType<T> type,
                                                            BiFunction<T, net.minecraft.core.Direction, IFluidHandler> getter) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, type, getter::apply);
    }
}
