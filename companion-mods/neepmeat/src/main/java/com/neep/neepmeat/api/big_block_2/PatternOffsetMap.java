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

package com.neep.neepmeat.api.big_block_2;

import com.neep.neepmeat.api.big_block.BigBlockPattern;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.Vec3i;

public class PatternOffsetMap<K>
{
    private final Map<K, Entry> map;

    public PatternOffsetMap(Supplier<Map<K, Entry>> supplier)
    {
        map = supplier.get();
    }

    public void put(K k, BigBlockPattern pattern)
    {
        map.put(k, new Entry(pattern, pattern.encode()));
    }

    public BigBlockPattern getPattern(K k)
    {
        return map.get(k).pattern;
    }

    public Int2ObjectMap<Vec3i> getOffsets(K k)
    {
        return map.get(k).offsets;
    }

    public record Entry(BigBlockPattern pattern, Int2ObjectMap<Vec3i> offsets)
    {

    }
}
