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

package com.neep.neepmeat.api.processing.random_ores;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class WeightedMap<T>
{
    private final NavigableMap<Float, T> entryMap = new TreeMap<>();
    private float weightTotal = 0;

    public void put(float weight, T entry)
    {
        if (weight == 0)
            return;

        weightTotal += weight;
        entryMap.put(weightTotal, entry);
    }

    public T get(Int2IntFunction random, float p)
    {
        if (entryMap.size() == 0)
            return null;

        if (weightTotal == -1)
        {
            weightTotal = 0;
            for (var entry : entryMap.keySet())
            {
                weightTotal += entry;
            }
        }

        p *= weightTotal;

        var mapEntry = entryMap.higherEntry(p);

        return mapEntry.getValue();
    }

    public void clear()
    {
        entryMap.clear();
        weightTotal = 0;
    }
}
