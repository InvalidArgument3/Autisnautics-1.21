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

package com.neep.neepmeat.transport.blood_network;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class PosDirectionMap<T>
{
    private final Class<T> clazz;
    private final Long2ObjectOpenHashMap<T[]> map = new Long2ObjectOpenHashMap<>();
//    private final HashMap<BlockPos, T[]> map = Maps.newHashMap();
    private List<T> listCache;
    boolean dirty = true;

    public PosDirectionMap(Class<T> clazz)
    {
        this.clazz = clazz;
    }

//    public void fastForEach(Consumer<Long2ObjectMap.Entry<T[]>> consumer)
    public void fastForEach(Consumer<Long2ObjectMap.Entry<T[]>> consumer)
    {
//        map.entrySet().forEach(e -> consumer.accept(new AbstractLong2ObjectMap.BasicEntry<>(e.getKey().asLong(), e.getValue())));
        map.long2ObjectEntrySet().fastForEach(consumer);
    }

    public Long2ObjectMap<T[]> map()
    {
        dirty = true;
        return null;
    }

    public int size()
    {
        return map.size();
    }

    public boolean isEmpty()
    {
        return map.isEmpty();
    }

    public void remove(long key)
    {
        map.remove(key);
        dirty = true;
    }

    public Iterable<T> get(long key)
    {
        var array = map.get(key);
        if (array == null)
            return Collections.emptyList();

        return () -> Arrays.stream(array).iterator();
    }

    public void put(long key, T[] array)
    {
        dirty = true;
//        map.put(BlockPos.fromLong(key), array);
        map.put(key, array);
    }

    public void clear()
    {
        map.clear();
        dirty = true;
    }

    public Stream<T> flatStream()
    {
        return map.values().stream().flatMap(Arrays::stream).filter(Objects::nonNull).distinct();
    }

    public void put(long key, int dir, T value)
    {
//        map.computeIfAbsent(BlockPos.fromLong(key), k -> (T[]) Array.newInstance(clazz, 6))[dir] = value;
        map.computeIfAbsent(key, k -> (T[]) Array.newInstance(clazz, 6))[dir] = value;
        dirty = true;
    }

    public List<T> asList()
    {
        if (dirty || listCache == null)
        {
            listCache = flatStream().toList();
        }
        return listCache;
    }

    public void forEach(Consumer<T> action)
    {
        flatStream().forEach(action);
    }
}
