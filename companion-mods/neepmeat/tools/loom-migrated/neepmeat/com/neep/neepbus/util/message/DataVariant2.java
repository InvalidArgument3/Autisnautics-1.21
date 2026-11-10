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

package com.neep.neepbus.util.message;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

// Uses a byte buffer. Does not implicitly convert between strings and numbers. This would be confusing to users.
public final class DataVariant2
{
    private final ByteBuffer bb;

    private DataVariant2(ByteBuffer bb)
    {
        this.bb = bb;
    }

    public static DataVariant2 of(float f)
    {
        ByteBuffer bb = ByteBuffer.allocate(Float.SIZE / 8);
        bb.putFloat(f);
        return new DataVariant2(bb);
    }

    public static DataVariant2 of(String s)
    {
        byte[] strBytes = s.getBytes();
        ByteBuffer bb = ByteBuffer.allocate(strBytes.length);
        bb.put(strBytes);
        return new DataVariant2(bb);
    }

    public static DataVariant2 of(int i)
    {
        ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / 8);
        bb.putInt(i);
        return new DataVariant2(bb);
    }

    public float asFloat()
    {
        return bb.getFloat(0);
    }

    public CharSequence asString()
    {
        return StandardCharsets.ISO_8859_1.decode(bb);
    }
}
