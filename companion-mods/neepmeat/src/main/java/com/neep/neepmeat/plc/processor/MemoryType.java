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

import com.neep.neepmeat.plc.memory.MemoryEntry;
import io.netty.buffer.ByteBuf;

import java.util.function.Function;

public enum MemoryType
{
    STRING(2, MemoryEntry.StringEntry::read),
    INTEGER(0, MemoryEntry.IntegerEntry::read),
//    ITEM_RESOURCE_AMOUNT(8, ItemRequestMemoryEntry::read),
    NULL(-1, bb -> null)
    ;

    public final byte code;
    public final Function<ByteBuf, MemoryEntry> create;

    public static MemoryType fromCode(byte code)
    {
        return switch (code)
        {
            case 0 -> INTEGER;
            case 2 -> STRING;
//            case 8 -> ITEM_RESOURCE_AMOUNT;
            default -> NULL;
        };
    }

    MemoryType(int code, Function<ByteBuf, MemoryEntry> create)
    {
        this.code = (byte) code;
        this.create = create;
    }

    // Hmmmmm, this may be limiting
    public boolean stackDirect()
    {
        return true;
    }
}
