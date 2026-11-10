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

package com.neep.neepmeat.transport.worm;

import com.neep.meatlib.storage.MeatlibStorageUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import java.util.List;
import java.util.function.Function;

public record DistributorParcel(List<ResourceAmount<ItemVariant>> items, List<ResourceAmount<FluidVariant>> fluids)
{
    public static DistributorParcel fromNbt(CompoundTag compound)
    {
        return new DistributorParcel(
                readList(compound.getList("items", Tag.TAG_COMPOUND), MeatlibStorageUtil::amountFromNbt),
                readList(compound.getList("fluids", Tag.TAG_COMPOUND), MeatlibStorageUtil::fluidAmountFromNbt));
    }

    public boolean isEmpty()
    {
        return items.isEmpty() && fluids.isEmpty();
    }

    public CompoundTag toNbt()
    {
        CompoundTag nbt = new CompoundTag();
        nbt.put("items", writeList(items, MeatlibStorageUtil::itemAmountToNbt));
        nbt.put("fluids", writeList(fluids, MeatlibStorageUtil::fluidAmountToNbt));
        return nbt;
    }

    public static <T> ListTag writeList(List<T> items, Function<T, CompoundTag> write)
    {
        ListTag list = new ListTag();
        for (var item : items)
        {
            list.add(write.apply(item));
        }
        return list;
    }

    public static <T> List<T> readList(ListTag list, Function<CompoundTag, T> read)
    {
        List<T> result = new ObjectArrayList<>();
        for (int i = 0; i < list.size(); i++)
        {
            CompoundTag compound = list.getCompound(i);
            result.add(read.apply(compound));
        }
        return result;
    }
}
