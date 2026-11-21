/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.plc.block;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface RedstoneInterface {
    BlockApiLookup<RedstoneInterface, Void> LOOKUP = BlockApiLookup.get(
            ResourceLocation.fromNamespaceAndPath("neepmeat", "redstone_interface"), RedstoneInterface.class, Void.class);

    int getReceivedStrength();

    void setEmittedStrength(int strength);

    static RedstoneInterface find(Level level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        return entity instanceof RedstoneInterface redstone ? redstone : null;
    }
}
