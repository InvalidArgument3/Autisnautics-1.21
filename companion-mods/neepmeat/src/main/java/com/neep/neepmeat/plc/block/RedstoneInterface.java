/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.plc.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface RedstoneInterface {
    int getReceivedStrength();

    void setEmittedStrength(int strength);

    static RedstoneInterface find(Level level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        return entity instanceof RedstoneInterface redstone ? redstone : null;
    }
}
