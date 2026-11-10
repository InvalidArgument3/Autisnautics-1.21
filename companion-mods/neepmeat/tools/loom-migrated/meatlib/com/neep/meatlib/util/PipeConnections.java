/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.meatlib.util;

import com.google.common.collect.Iterables;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.core.Direction;

public class PipeConnections implements PipeConnectionsC
{
    public static final PipeConnectionsC EMPTY = new PipeConnections(EnumSet.noneOf(Direction.class));
    public static final PipeConnectionsC ALL = new PipeConnections(EnumSet.allOf(Direction.class));

    // TODO: use a byte instead of an EnumSet and remove all those pesky array indirections
    // More pointless optimisation!
    private final EnumSet<Direction> set;

    public PipeConnections()
    {
        this(EnumSet.noneOf(Direction.class));
    }

    public PipeConnections(EnumSet<Direction> set)
    {
        this.set = EnumSet.copyOf(set);
    }

    public PipeConnections(Set<Direction> set)
    {
        this.set = EnumSet.copyOf(set);
    }

    public PipeConnections(PipeConnections other)
    {
        this(EnumSet.copyOf(other.set));
    }

    public PipeConnections(Direction face)
    {
        this(EnumSet.of(face));
    }

    public PipeConnections set(int dir, boolean connect)
    {
        set(Direction.values()[dir], connect);
        return this;
    }

    public PipeConnections set(Direction face, boolean connect)
    {
        if (connect)
            set.add(face);
        else
            set.remove(face);

        return this;
    }

    @Override
    public PipeConnections with(Direction face, boolean connect)
    {
        var next = new PipeConnections(set);
        next.set(face, connect);
        return next;
    }

    public PipeConnections with(int dir, boolean connect)
    {
        return with(Direction.values()[dir], connect);
    }

    public PipeConnections with(Direction face)
    {
        return with(face, true);
    }

    public boolean contains(Direction face)
    {
        return set.contains(face);
    }

    public boolean contains(int dir)
    {
        return set.contains(Direction.values()[dir]);
    }

    public PipeConnections set(Direction face)
    {
        set.add(face);
        return this;
    }

    public PipeConnections unset(Direction face)
    {
        set.remove(face);
        return this;
    }

    public PipeConnections set(int dir)
    {
        set(Direction.values()[dir]);
        return this;
    }

    public PipeConnections unset(int dir)
    {
        unset(Direction.values()[dir]);
        return this;
    }

    public int size()
    {
        return set.size();
    }

    public Iterable<Direction> directions()
    {
        return set;
    }

    public boolean isEmpty()
    {
        return set.isEmpty();
    }

    public Direction get(int i)
    {
        return Iterables.get(set, i);
    }

    @Override
    public PipeConnections copy()
    {
        return new PipeConnections(this);
    }

    public void unsetAll(Set<Direction> excluded)
    {
        set.removeAll(excluded);
    }

    @Override
    public @NotNull Iterator<Direction> iterator()
    {
        return set.iterator();
    }
}
