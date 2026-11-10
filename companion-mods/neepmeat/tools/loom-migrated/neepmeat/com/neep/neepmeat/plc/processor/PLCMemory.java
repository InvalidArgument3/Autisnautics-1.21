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

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.api.plc.memory.MemoryManager;
import com.neep.neepmeat.neepasm.vm.ThordData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;

public class PLCMemory implements MemoryManager, NbtSerialisable
{
    private final int maxSize = 32;

    private final ObjectArrayList<ThordData> entries = new ObjectArrayList<>(maxSize);
    private final byte[] table = new byte[maxSize];

    private final VariableStack stack;

    public PLCMemory(VariableStack stack)
    {
        this.stack = stack;
        clear();
        entries.size(maxSize);
    }

    @Override
    public void clear()
    {
        entries.clear();
        entries.size(maxSize);
        Arrays.fill(table, (byte)0);

        // Keep the first element null as 0 is reserved for null
        entries.add(null);
    }

    @Override
    public void store(int address, ThordData data) throws NeepASM.RuntimeException
    {
        int stripped = Prefix.strip(address);
        assertSize(stripped, "STORE");
        entries.set(stripped, data.copy());
        if (table[stripped] == 0)
            table[stripped] = 1;
    }

    @Override
    public ThordData allocate(ThordData entry) throws NeepASM.RuntimeException
    {
        for (int i = 1; i <= entries.size(); i++)
        {
            if (entries.get(i) == null)
            {
                entries.set(i, entry);
                table[i] = 1;
            }
//            if (i == maxSize)
//            {
////                gc();
//                i = 0;
//            }
//            if (i == entries.size())
//            {
//                size(entries.size() + 1);
//                entries.add(i, entry);
//                return new ThordData.PointerImpl(i);
//            }
//            else if (entries.get(i) == null)
//            {
//                entries.set(i, entry);
//                return new ThordData.PointerImpl(i);
//            }
        }

        return new ThordData.PointerImpl(NULL);
    }

    @Override
    public int allocateSize(int size) throws NeepASM.RuntimeException
    {
        int count = 0;
        for (int i = 1; i < entries.size(); i++)
        {
            if (entries.get(i) == null)
            {
                count++;
            }

            if (count == size)
            {
                int best = i - size + 1;
                for (int j = best; j < best + size; j++)
                {
                    entries.set(j, ThordData.NULL);
                    table[j] = 0;
                }
                table[best] = (byte) size;
                return best;
            }
        }

        throw new NeepASM.RuntimeException("Out of memory");
    }

    private void size(int size) throws NeepASM.RuntimeException
    {
        if (size > maxSize)
            throw new NeepASM.RuntimeException(String.format("Out of memory: address %s is greater than %s", size, maxSize));

        entries.size(size);
    }

    @Override
    public void free(int pointer) throws NeepASM.RuntimeException
    {
        assertSize(pointer, "FREE");
        int size = getSize(pointer);
        if (pointer > 0 && pointer < entries.size())
        {
            for (int i = pointer; i < pointer + size; i++)
            {
                table[i] = 0;
                entries.set(i, null);
            }
        }
    }

    int getSize(int address)
    {
        if (address < 0 || address > table.length)
            return 0;

        return table[address];
    }

    @Override
    public ThordData fetch(int address)
    {
        Prefix prefix = Prefix.get(address);
        return switch (prefix)
        {
            case PROGRAM -> ThordData.NULL;
            case PROGRAM_DATA -> ThordData.NULL;
            case VARIABLE -> getInternal(Prefix.strip(address));
            case RESERVED -> ThordData.NULL;
        };
    }

    private @NotNull ThordData getInternal(int address)
    {
        if (address < entries.size())
        {
            return Objects.requireNonNullElse(entries.get(address).copy(), ThordData.NULL);
        }

        return ThordData.NULL;
    }

    public void gc() throws NeepASM.RuntimeException
    {
        IntSet pointers = new IntOpenHashSet();
        for (int i = 0; i < stack.size(); i++)
        {
            var entry = stack.peekUnsafe(i);
            if (entry.pointer())
                pointers.add(entry.intValue());
        }

        for (int i = entries.size() - 1; i >= 0; --i)
        {
            if (!pointers.contains(i))
            {
                entries.set(i, null);
            }
        }
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        // NBT is bloat
        // Why not just serialise directly to bytes?
        // It's a computer. Computers have memory that consists of a contiguous array of cells.
        // Computers do not store memory in tree structures based on hash tables
        ByteBuf bb = Unpooled.buffer();
        write(bb);
        byte[] bytes = new byte[bb.writerIndex()];
        bb.getBytes(0, bytes); // Avoid serialising more bytes than are used
        nbt.putByteArray("memory", bytes);
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        var memory = nbt.getByteArray("memory");

        ByteBuf bb = Unpooled.wrappedBuffer(memory);
        read(bb);
    }

    private void assertSize(int offset, String ctx) throws NeepASM.RuntimeException
    {
        if (offset < 0 || offset >= entries.size())
            throw new NeepASM.RuntimeException("%s: Memory address %s out of bounds".formatted(ctx, offset));
    }

    public void write(ByteBuf bb)
    {
        bb.writeInt(101); // version
        bb.writeBytes(table);
        for (var entry : entries)
        {
            if (entry != null)
            {
                bb.writeBoolean(true);
                ThordData.write(entry, bb);
            }
            else
            {
                bb.writeBoolean(false);
            }
        }
    }

    public void read(ByteBuf bb)
    {
        entries.clear();
        entries.size(maxSize);
        if (bb.capacity() > Integer.BYTES)
        {
            int version = bb.readInt();
            if (version == 101)
            {
                bb.readBytes(table);
                for (int i = 0; i < maxSize; i++)
                {
                    if (bb.readBoolean())
                        entries.set(i, ThordData.read(bb));
                    else
                        entries.set(i, null);
                }
            }
        }
    }

    public enum Prefix
    {
        VARIABLE(0),
        PROGRAM(1),
        PROGRAM_DATA(2),
        RESERVED(3) // In this case, 'reserved' means 'I want there to be four possible values'
        ;

        // Treat addresses as 16-bit numbers where the first two bits are the prefix.
        public static final int ADDRESS_BITS = 14;
        public static final int ADDRESS_MASK = (1 << ADDRESS_BITS) - 1; // Should be 0x3FFF

        public static Prefix get(int address)
        {
            int prefix = (address >> ADDRESS_BITS) & 0b11;
            return Prefix.values()[prefix];
        }

        public final int prefix;

        Prefix(int prefix)
        {
            this.prefix = prefix;
        }

        public static int strip(int address)
        {
            return address & ADDRESS_MASK;
        }

        public int encode(int address)
        {
            return (address & ADDRESS_MASK) | prefix << ADDRESS_BITS;
        }
    }
}
