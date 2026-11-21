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

package com.neep.neepmeat.util;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public abstract class DFSFinder<T>
{
    private final Deque<BlockPos> posStack = new ArrayDeque<>();
    private final Deque<Direction> dirStack = new ArrayDeque<>();
    private final Set<Long> visited = new LongOpenHashSet();
    private Pair<BlockPos, T> result;
    private State state;

    public DFSFinder()
    {
    }

    protected void setResult(BlockPos pos, T result)
    {
        this.result = Pair.of(pos.immutable(), result);
    }

    public Pair<BlockPos, T> getResult()
    {
        return result;
    }

    protected boolean visited(BlockPos pos)
    {
        return visited.contains(pos.asLong());
    }

    protected void setVisited(BlockPos pos)
    {
        visited.add(pos.asLong());
    }

    public void reset()
    {
        posStack.clear();
        dirStack.clear();
        visited.clear();
        result = null;
    }

    public void pushBlock(BlockPos pos, Direction direction)
    {
        posStack.push(pos.immutable());
        dirStack.push(direction);
    }

    public BlockPos popBlock()
    {
        return posStack.pop();
    }

    public Direction popDir()
    {
        return dirStack.pop();
    }

    public void prepare(BlockPos start, Direction startDir)
    {
        pushBlock(start, startDir);
    }

    public void loop(int maxDepth)
    {
        int depth = 0;
        while (depth < maxDepth && propagate())
        {
            ++depth;
        }
    }

    public boolean propagate()
    {
        if (!posStack.isEmpty())
        {
            // This method should push one adjacent vertex
            state = processPos(posStack.peek(), dirStack.peek());
            return state == State.CONTINUE;
        }
        return false;
    }

//    public State getState()
//    {
//        return state;
//    }
//
//    protected void setState(State state)
//    {
//        this.state = state;
//    }

    protected abstract State processPos(BlockPos current, Direction fromDir);

    public boolean hasResult()
    {
        return result != null;
    }

    public enum State
    {
        CONTINUE,
        REVERSE,
        FAIL,
        SUCCESS;
    }
}
