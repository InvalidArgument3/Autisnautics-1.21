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

package com.neep.neepmeat.api.live_machine.metrics;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;

public class LongDataBuffer extends LongArrayFIFOQueue
{
    private final int capacity;

    public LongDataBuffer(int capacity)
    {
        super(capacity);
        this.capacity = capacity;
    }

    public void enqueueCircular(long f)
    {
        enqueue(f);
        if (size() >= capacity)
            dequeueLong();
    }

    public long[] encode()
    {
        LongArrayList longs = new LongArrayList();

        int i = start;
        while (true)
        {
            // end is always shorter than length
            if (i == array.length)
                i = 0;

            if (i == end)
                break;

            longs.add(array[i]);
            i++;

        }
        return longs.elements();
    }
}
