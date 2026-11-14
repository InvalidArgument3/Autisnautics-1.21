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
import com.neep.neepmeat.neepasm.vm.ThordData;
import com.neep.neepmeat.plc.processor.MemoryType;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;

public interface MemoryEntry
{
    MemoryEntry NULL = new IntegerEntry(0);

    @Nullable
    static MemoryEntry read(ByteBuf bb)
    {
        var type = MemoryType.fromCode(bb.readByte());
        return type.create.apply(bb);
    }

    static MemoryEntry of(DataVariant data)
    {
        if (data.isNumeric())
            return new IntegerEntry(data.getAsInt()); // TODO: float

        return new StringEntry(data.stringValue());
    }

    static void write(@Nullable MemoryEntry entry, ByteBuf bb)
    {
        if (entry == null)
        {
            bb.writeByte(-1);
        }
        else
        {
            bb.writeByte(entry.type().code);
            entry.write(bb);
        }
    }

    MemoryType type();

    void write(ByteBuf bb);

    DataVariant toDataVariant();

    ThordData toThordData();

    record IntegerEntry(int value) implements MemoryEntry
    {
        @Override
        public MemoryType type()
        {
            return MemoryType.INTEGER;
        }

        @Override
        public void write(ByteBuf bb)
        {
            bb.writeInt(value);
        }

        @Override
        public DataVariant toDataVariant()
        {
            return DataVariant.of(value);
        }

        @Override
        public ThordData toThordData()
        {
            return new ThordData.IntImpl(value);
        }

        @Override
        public @NotNull String toString()
        {
            return String.valueOf(value);
        }

        public static MemoryEntry read(ByteBuf bb)
        {
            return new IntegerEntry(bb.readInt());
        }
    }

    record StringEntry(String value) implements MemoryEntry
    {
        @Override
        public MemoryType type()
        {
            return MemoryType.STRING;
        }

        @Override
        public void write(ByteBuf bb)
        {
            bb.writeInt(value.length());
            bb.writeCharSequence(value, StandardCharsets.ISO_8859_1);
        }

        @Override
        public DataVariant toDataVariant()
        {
            return DataVariant.of(value);
        }

        @Override
        public ThordData toThordData()
        {
            return new ThordData.StringImpl(value);
        }

        @Override
        public @NotNull String toString()
        {
            return value;
        }

        public static MemoryEntry read(ByteBuf bb)
        {
            int length = bb.readInt();
            return new StringEntry(bb.readCharSequence(length, StandardCharsets.ISO_8859_1).toString());
        }
    }
}
