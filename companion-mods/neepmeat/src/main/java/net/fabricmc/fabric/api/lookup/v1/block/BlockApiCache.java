/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */


package net.fabricmc.fabric.api.lookup.v1.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockApiCache<A, C> {
    public static <A, C> BlockApiCache<A, C> create(BlockApiLookup<A, C> lookup, ServerLevel world, BlockPos pos) {
        return new BlockApiCache<>();
    }
    public A find(C context) { return null; }
    public A find(BlockState state, C context) { return null; }
    public A get(C context) { return null; }
}
