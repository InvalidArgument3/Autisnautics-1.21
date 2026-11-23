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

package com.neep.neepbus.util;

import com.neep.neepbus.util.message.DataVariant;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class DirectReadPort implements ReadPort
{
    private final ConfigEntry entry;
    private final BiConsumer<String, DataVariant> sender;
    private final Supplier<DataVariant> read;

    public DirectReadPort(ConfigEntry entry, Supplier<DataVariant> read, BiConsumer<String, DataVariant> sender)
    {
        this.entry = entry;
        this.read = read;
        this.sender = sender;
    }

//    public DirectReadPort(ConfigEntry entry, ObjIntConsumer<String> sender, DoubleSupplier read)
//    {
//        this.entry = entry;
//        this.sender = sender;
//
//        // A stopgap until we decide whether to handle doubles instead of ints.
//        this.read = () -> (int) Math.round(read.getAsDouble());
//    }

    public void send()
    {
        sender.accept(entry.getAddress(), read.get());
    }

//    public void send(double data)
//    {
//        send((int) Math.round(data));
//    }

    @Override
    public DataVariant read()
    {
        return read.get();
    }

    public ConfigEntry entry()
    {
        return entry;
    }
}
