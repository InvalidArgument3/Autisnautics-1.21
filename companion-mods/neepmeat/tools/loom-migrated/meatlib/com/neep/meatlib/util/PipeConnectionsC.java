package com.neep.meatlib.util;

import net.minecraft.core.Direction;

public interface PipeConnectionsC extends Iterable<Direction>
{
    PipeConnections with(Direction face, boolean connect);
    PipeConnections with(int dir, boolean connect);
    PipeConnections with(Direction face);
    boolean contains(Direction face);
    boolean contains(int dir);
    int size();
    Iterable<Direction> directions();
    boolean isEmpty();
    Direction get(int i);

    PipeConnections copy();
}
