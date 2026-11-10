/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.registry;

import com.neep.meatlib.MeatLib;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;

/**
 * Proves deferred registration. No item, so it stays out of the creative search tab.
 */
public final class MeatlibProbe {
    public static final RegistrationContext CONTEXT = new RegistrationContext(MeatLib.ID);
    public static final DeferredBlock<Block> PROBE = CONTEXT.block("probe",
            () -> new Block(BlockBehaviour.Properties.of().strength(1.0F)));

    private MeatlibProbe() {}
}
