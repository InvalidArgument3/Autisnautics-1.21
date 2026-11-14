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

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.floats.FloatArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class FloatDataBuffer extends FloatArrayFIFOQueue
{
    private final int capacity;

    public FloatDataBuffer(int capacity)
    {
        super(capacity);
        this.capacity = capacity;
    }

    public void enqueueCircular(float f)
    {
        enqueue(f);
        int s = size();
        if (size() >= capacity)
        {
            float f1 = dequeueFloat();
        }
    }

    public int[] encode()
    {
        return getDoubles().doubleStream().mapToInt(d -> Float.floatToIntBits((float) d)).toArray();
    }

    public double[] toDoubleArray()
    {
        return getDoubles().elements();
    }

    private DoubleArrayList getDoubles()
    {
        DoubleArrayList list = new DoubleArrayList();

        int i = start;
        while (i != end)
        {
            // end is always shorter than length
            if (i == array.length)
                i = 0;

            if (i == end)
                break;

            list.add(array[i]);
            i++;
        }
        return list;
    }
}
