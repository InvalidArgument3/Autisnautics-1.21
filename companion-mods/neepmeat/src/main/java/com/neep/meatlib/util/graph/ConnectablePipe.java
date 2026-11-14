/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.util.graph;

import com.neep.meatlib.util.PipeConnectionsC;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface ConnectablePipe {
    PipeConnectionsC getConnections(BlockState state);

    boolean isConnected(BlockGetter level, BlockPos pos, BlockState state, Direction direction);

    default int countConnections(BlockState state) {
        return getConnections(state).size();
    }

    PipeVertex getPipeVertex(ServerLevel level, BlockPos pos, BlockState state);

    interface Find {
        ConnectablePipe find(ServerLevel level, BlockPos pos, BlockState state);
    }
}
