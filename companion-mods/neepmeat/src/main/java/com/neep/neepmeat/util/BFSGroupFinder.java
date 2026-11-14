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

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import net.minecraft.core.BlockPos;

public abstract class BFSGroupFinder<T>
{
    protected final Queue<BlockPos> posQueue = new LinkedList<>();
    protected final LongSet visited = new LongOpenHashSet();
    private final Map<BlockPos, T> result = new HashMap<>();

    public BFSGroupFinder()
    {
    }

    protected void addResult(BlockPos pos, T result)
    {
        this.result.put(pos.immutable(), result);
    }

    public Map<BlockPos, T> getResult()
    {
        return result;
    }

    public void reset()
    {
        posQueue.clear();
        visited.clear();
        result.clear();
    }

    public BFSGroupFinder<T> queueBlock(BlockPos pos)
    {
        if (!visited.contains(pos.asLong()))
        {
            posQueue.add(pos.immutable());
            visited.add(pos.asLong());
        }
        return this;
    }

    public BFSGroupFinder<T> loop(int maxDepth)
    {
        int depth = 0;
        while (propagate(maxDepth) && depth < maxDepth)
        {
            ++depth;
        }
        return this;
    }

    public boolean propagate(int maxDepth)
    {
        if (!posQueue.isEmpty())
        {
            BlockPos current = posQueue.poll();
            State state = processPos(current);
            return state == State.CONTINUE;
        }
        return false;
    }

    protected abstract State processPos(BlockPos pos);

    public enum State
    {
        CONTINUE,
        SUCCESS,
        FAIL;
    }
}
