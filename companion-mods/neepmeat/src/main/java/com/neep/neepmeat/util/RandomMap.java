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

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class RandomMap<K, V>
{
    protected Object2ObjectMap<K, V> byId = new Object2ObjectOpenHashMap<>();
    protected NavigableMap<Float, V> byWeight = new TreeMap<>();
    protected Random random = new Random();

    protected float totalWeight;

    public void put(K k, V v, float weight)
    {
        totalWeight += weight;
        byId.put(k, v);
        byWeight.put(totalWeight, v);
    }

    public V get(K k)
    {
        return byId.get(k);
    }

    public V next()
    {
        float value = random.nextFloat() * totalWeight;
        return byWeight.higherEntry(value).getValue();
    }

    public Object2ObjectMap<K, V> getById()
    {
        return byId;
    }
}
