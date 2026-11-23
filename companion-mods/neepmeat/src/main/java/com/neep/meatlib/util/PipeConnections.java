/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port of the upstream connection set. GPL-3.0-or-later headers.
 */

package com.neep.meatlib.util;

import com.google.common.collect.Iterables;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

public class PipeConnections implements PipeConnectionsC {
    public static final PipeConnectionsC EMPTY = new PipeConnections(EnumSet.noneOf(Direction.class));
    public static final PipeConnectionsC ALL = new PipeConnections(EnumSet.allOf(Direction.class));

    private final EnumSet<Direction> set;

    public PipeConnections() {
        this(EnumSet.noneOf(Direction.class));
    }

    public PipeConnections(EnumSet<Direction> set) {
        this.set = EnumSet.copyOf(set);
    }

    public PipeConnections(PipeConnections other) {
        this(EnumSet.copyOf(other.set));
    }

    public PipeConnections set(Direction face, boolean connect) {
        if (connect) {
            set.add(face);
        } else {
            set.remove(face);
        }
        return this;
    }

    public PipeConnections set(Direction face) {
        set.add(face);
        return this;
    }

    @Override
    public PipeConnections with(Direction face, boolean connect) {
        PipeConnections next = new PipeConnections(set);
        next.set(face, connect);
        return next;
    }

    @Override
    public PipeConnections with(int dir, boolean connect) {
        return with(Direction.values()[dir], connect);
    }

    @Override
    public PipeConnections with(Direction face) {
        return with(face, true);
    }

    @Override
    public boolean contains(Direction face) {
        return set.contains(face);
    }

    @Override
    public boolean contains(int dir) {
        return set.contains(Direction.values()[dir]);
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public Iterable<Direction> directions() {
        return set;
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public Direction get(int index) {
        return Iterables.get(set, index);
    }

    @Override
    public PipeConnections copy() {
        return new PipeConnections(this);
    }

    @Override
    public @NotNull Iterator<Direction> iterator() {
        return set.iterator();
    }
}
