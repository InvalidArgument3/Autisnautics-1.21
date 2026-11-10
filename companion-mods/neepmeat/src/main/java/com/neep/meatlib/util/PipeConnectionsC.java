/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.util;

import net.minecraft.core.Direction;

public interface PipeConnectionsC extends Iterable<Direction> {
    PipeConnections with(Direction face, boolean connect);

    PipeConnections with(int dir, boolean connect);

    PipeConnections with(Direction face);

    boolean contains(Direction face);

    boolean contains(int dir);

    int size();

    Iterable<Direction> directions();

    boolean isEmpty();

    Direction get(int index);

    PipeConnections copy();
}
