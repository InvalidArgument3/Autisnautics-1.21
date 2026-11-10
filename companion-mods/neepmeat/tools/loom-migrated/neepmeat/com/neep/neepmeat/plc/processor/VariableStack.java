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

package com.neep.neepmeat.plc.processor;

import com.neep.meatlib.util.NbtSerialisable;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.vm.DataStack;
import com.neep.neepmeat.neepasm.vm.ThordData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;

public class VariableStack implements DataStack, NbtSerialisable
{
    private final ObjectArrayList<ThordData> data = new ObjectArrayList<>();

    private final int maxSize;
    private final String name;

    public VariableStack(int maxSize, String name)
    {
        this.maxSize = maxSize;
        this.name = name;
    }

    @Override
    public void push(ThordData entry) throws NeepASM.RuntimeException
    {
        if (size() >= maxSize)
            throw new NeepASM.RuntimeException("%s overflow".formatted(name));

        data.push(entry);
    }

    @Override
    public ThordData pop() throws NeepASM.RuntimeException
    {
        if (isEmpty())
            throw new NeepASM.RuntimeException("%s underflow".formatted(name));

        return data.pop();
    }

    @Override
    public void set(int i, ThordData of) throws NeepASM.RuntimeException
    {
        i = size() - 1 - i;
        if (i < 0 || i >= data.size())
            throw new NeepASM.RuntimeException("%s out of bounds".formatted(name));

        data.set(i, of);
    }

    @Override
    public ThordData peekUnsafe(int i) throws NeepASM.RuntimeException
    {
        if (i >= size() || i < 0)
            throw new NeepASM.RuntimeException("%s underflow".formatted(name));

        return data.peek(i);
    }

    @Override
    public int size()
    {
        return data.size();
    }

    @Override
    public boolean isEmpty()
    {
        return data.isEmpty();
    }

    private void write(ByteBuf bb)
    {
        bb.writeInt(data.size());
        for (var entry : data)
        {
            ThordData.write(entry, bb);
        }
    }

    private void read(ByteBuf bb)
    {
        data.clear();
        if (bb.capacity() > Integer.BYTES)
        {
            int size = bb.readInt();
            for (int i = 0; i < size; i++)
            {
                data.add(ThordData.read(bb));
            }
        }
    }

    public void clear()
    {
        data.clear();
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        ByteBuf bb = Unpooled.buffer();
        write(bb);
        byte[] bytes = new byte[bb.writerIndex()];
        bb.getBytes(0, bytes);

        nbt.putByteArray("data", bytes);
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        ByteBuf bb = Unpooled.wrappedBuffer(nbt.getByteArray("data"));
        read(bb);
    }
}
