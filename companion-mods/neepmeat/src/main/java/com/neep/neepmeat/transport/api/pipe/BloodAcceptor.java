/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.transport.api.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public interface BloodAcceptor {
    static @Nullable BloodAcceptor find(Level level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        return entity instanceof BloodAcceptor acceptor ? acceptor : null;
    }

    /** Upstream name is {@code getResultItem}. */
    default long output() {
        return 0;
    }

    default float updateInflux(float influx) {
        return influx;
    }

    Mode mode();

    enum Mode {
        SINK,
        SOURCE,
        ACTIVE_SINK,
        BALANCE
    }
}
