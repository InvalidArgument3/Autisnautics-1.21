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

package com.neep.neepmeat.plc.memory;

import com.neep.neepbus.util.message.DataVariant;
import com.neep.neepmeat.neepasm.vm.WorldTargetImpl;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import net.minecraft.util.Mth;

public class ResizableByteBuffer
{
    private ByteBuffer bb;
    private int size;

    public ResizableByteBuffer(int capacity)
    {
        this.size = Mth.smallestEncompassingPowerOfTwo(capacity);
        this.bb = ByteBuffer.allocate(size);
    }

    public ResizableByteBuffer(ByteBuffer bb)
    {
        this.size = bb.capacity();
        this.bb = bb;
    }

    public ResizableByteBuffer(ResizableByteBuffer other)
    {
        this(other.bb.duplicate());
    }

    public void assureSize(int i, int elementSize)
    {
        int requiredCapacity = i + elementSize;
        if (requiredCapacity > bb.capacity())
        {
            ByteBuffer newBuffer = ByteBuffer.allocate(Mth.smallestEncompassingPowerOfTwo(requiredCapacity));
            newBuffer.put(bb);
            bb = newBuffer;
        }

        if (requiredCapacity > size)
            this.size = requiredCapacity;
    }

    public void assureSize(int size)
    {
        assureSize(size, 0);
    }

    public void putByte(int offset, byte value)
    {
        assureSize(offset, 1);
        bb.put(offset, value);
    }

    public void putFloat(int i, float val)
    {
        assureSize(i, Float.SIZE / 8);
        bb.putFloat(i, val);
    }

    public void putInt(int i, int val)
    {
        assureSize(i, Integer.SIZE / 8);
        bb.putInt(i, val);
    }

    public void putString(int i, String val)
    {
        var bytes = val.getBytes(StandardCharsets.ISO_8859_1);
        assureSize(i, bytes.length);
        bb.put(i, bytes);
    }

    public float getFloat(int i)
    {
        return bb.getFloat(i);
    }

    public int getInt(int i)
    {
        return bb.getInt(i);
    }

    public byte getByte(int i)
    {
        return bb.get(i);
    }

    public CharSequence getString(int offset, int bytes)
    {
//        var array = new byte[bytes];
//        bb.get(offset, array);
        return StandardCharsets.ISO_8859_1.decode(ByteBuffer.wrap(bb.array(), offset, bytes));
    }

    public void clear()
    {
        bb.clear();
    }

    public int capacity()
    {
        return bb.capacity();
    }

    public int size()
    {
        return size;
    }

    public void erase(int offset, int size)
    {
        Arrays.fill(bb.array(), offset, offset + size, (byte) 0);
        if (offset + size >= this.size)
        {
            this.size -= size;
        }
    }

    public byte[] trimmed()
    {
        byte[] array = new byte[size];
        bb.get(array);
        return array;
    }

    public static abstract class Entry implements DataVariant
    {
        protected final ResizableByteBuffer bb;
        public final int offset;

        protected Entry(ResizableByteBuffer bb, int offset)
        {
            this.bb = bb;
            this.offset = offset;
        }

        public float eraseFloat()
        {
            float value = floatValue();
            erase();
            return value;
        }

        public int eraseInt()
        {
            int value = intValue();
            erase();
            return value;
        }

        public String eraseString()
        {
            String value = stringValue();
            erase();
            return value;
        }

        public abstract void erase();
    }

    public static class FloatEntry extends Entry
    {
        protected FloatEntry(ResizableByteBuffer bb, int offset, float value)
        {
            super(bb, offset);
            bb.putFloat(offset, value);
        }

        @Override
        public float floatValue()
        {
            return bb.getFloat(offset);
        }

        @Override
        public String stringValue()
        {
            return Float.toString(floatValue());
        }

        @Override
        public WorldTargetImpl wtValue()
        {
            return null;
        }

        @Override
        public boolean boolValue()
        {
            return floatValue() != 0;
        }

        @Override
        public void erase()
        {
            bb.erase(offset, Float.BYTES);
        }
    }

    public static class IntEntry extends Entry
    {
        public IntEntry(ResizableByteBuffer bb, int offset, int value)
        {
            super(bb, offset);
            bb.putInt(offset, value);
        }

        @Override
        public int intValue()
        {
            return bb.getInt(offset);
        }


        @Override
        public float floatValue()
        {
            return intValue();
        }

        @Override
        public String stringValue()
        {
            return Integer.toString(intValue());
        }

        @Override
        public WorldTargetImpl wtValue()
        {
            return null;
        }

        @Override
        public boolean boolValue()
        {
            return intValue() != 0;
        }

        @Override
        public void erase()
        {
            bb.erase(offset, Integer.BYTES);
        }
    }

    public static class StringEntry extends Entry
    {
        private final int size;

        protected StringEntry(ResizableByteBuffer bb, int offset, String s)
        {
            super(bb, offset);
            this.size = s.length();
            bb.putString(offset, s);
        }

        @Override
        public float floatValue()
        {
            return DataVariant.toFloat(stringValue());
        }

        @Override
        public String stringValue()
        {
            return bb.getString(offset, size).toString();
        }

        @Override
        public WorldTargetImpl wtValue()
        {
            return null;
        }

        @Override
        public boolean boolValue()
        {
            return !stringValue().isEmpty();
        }

        @Override
        public void erase()
        {
            bb.erase(offset, size);
        }
    }
}
