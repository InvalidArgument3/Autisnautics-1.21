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

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;

public class Pos2ObjectOpenHashMap<V> extends Long2ObjectOpenHashMap<V>
{
    public V get(BlockPos pos)
    {
        return super.get(pos.asLong());
    }

    public V put(BlockPos pos, V v)
    {
        return super.put(pos.asLong(), v);
    }

    public V putIfAbsent(BlockPos pos, V v)
    {
        return super.putIfAbsent(pos.asLong(), v);
    }

    public V getOrDefault(BlockPos pos, V defaultValue)
    {
        return super.getOrDefault(pos.asLong(), defaultValue);
    }
}
