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

import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepbus.util.message.DataVariant;
import java.util.function.BiConsumer;
import net.minecraft.nbt.CompoundTag;

/**
 * Remembers the last sent value and allows it to be read from elsewhere.
 * Don't forget to save NBT,
 */
public class SimpleReadPort implements ReadPort, NbtSerialisable
{
    private final ConfigEntry entry;
    private final BiConsumer<String, DataVariant> sender;

    private DataVariant lastValue = DataVariant.NULL;

    public SimpleReadPort(ConfigEntry entry, BiConsumer<String, DataVariant> sender)
    {
        this.entry = entry;
        this.sender = sender;
    }

    public void send(DataVariant data)
    {
        lastValue = data;
        sender.accept(entry.getAddress(), data);
    }

    public void send(float data)
    {
        send(DataVariant.of(data));
    }

    public void send()
    {
        sender.accept(entry.getAddress(), lastValue);
    }

    public void set(DataVariant data)
    {
        this.lastValue = data;
    }
//    public void send(double data)
//    {

//        send((int) Math.round(data));

//    }

    @Override
    public DataVariant read()
    {
        return lastValue;
    }

    public ConfigEntry entry()
    {
        return entry;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.putString("last_value", lastValue.toString());
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        this.lastValue = DataVariant.of(nbt.getString("last_value"));
    }

    public DataVariant getValue()
    {
        return lastValue;
    }
}
