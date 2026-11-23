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

package com.neep.neepmeat.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("UnstableApiUsage")
public class DataVariantImpl implements DataResource
{
    private final DataType data;

    private static final Logger LOGGER = LogManager.getLogger("neepmeat/data");

    private DataVariantImpl(DataType data)
    {
        this.data = data;
    }

    @Override
    public boolean isBlank()
    {
        return data == DataType.BLANK;
    }

    @Override
    public DataType getObject()
    {
        return data;
    }

    @Override
    public @Nullable CompoundTag getNbt()
    {
        return null;
    }

    @Override
    public CompoundTag toNbt()
    {
        CompoundTag result = new CompoundTag();

        result.putString("id", DataType.REGISTRY.getKey(data).toString());

        return result;
    }

    public static DataResource fromNbt(CompoundTag nbt)
    {
        try
        {
            return DataResource.of(DataType.REGISTRY.get(ResourceLocation.tryParse(nbt.getString("id"))));
        }
        catch (RuntimeException runtimeException)
        {
            LOGGER.debug("Tried to load an invalid DataVariant from NBT: {}", nbt, runtimeException);
            return DataResource.BLANK;
        }
    }

    public static DataResource of(DataType data)
    {
        Objects.requireNonNull(data, "Item may not be null.");
        return new DataVariantImpl(data);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof DataVariantImpl other)
        {
            return other.data == data;
        }
        return false;
    }

    @Override
    public void toPacket(FriendlyByteBuf buf)
    {
        buf.writeBoolean(!isBlank());
    }
}
