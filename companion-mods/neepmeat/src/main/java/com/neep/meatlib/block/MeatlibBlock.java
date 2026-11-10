/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.block;

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public interface MeatlibBlock {
    BlockItem blockItem();

    @FunctionalInterface
    interface ItemFactory {
        BlockItem create(Block block, RegistrationContext context, ItemSettings settings);
    }
}
