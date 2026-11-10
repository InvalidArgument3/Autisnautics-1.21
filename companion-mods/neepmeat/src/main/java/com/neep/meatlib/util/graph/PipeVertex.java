/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.util.graph;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public interface PipeVertex {
    boolean canSimplify();

    void setAdjVertex(int id, PipeVertex vertex);

    interface Lookup<T extends PipeVertex> {
        T find(ServerLevel level, BlockPos pos);
    }
}
