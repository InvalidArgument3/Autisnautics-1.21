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

package com.neep.neepmeat.item.filter;

import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.util.NbtSerialisable;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

public class FilterList implements NbtSerialisable
{
    public static final ParamCodec<FilterList> CODEC = ParamCodec.of(FilterList.class,
        FilterList::write, FilterList::read);

    protected final List<Entry> entries = new ObjectArrayList<>();
    protected final int maxEntries;
    private final Runnable markDirty;

    public FilterList(int maxEntries, Runnable markDirty)
    {
        this.maxEntries = maxEntries;
        this.markDirty = markDirty;
    }

    public boolean matches(ItemVariant variant)
    {
        if (isEmpty())
            return true;

        for (var entry : entries)
        {
            if (entry.matches(variant))
                return true;
        }

        return false;
    }

    public void add(Filter filter)
    {
        if (entries.size() < maxEntries)
            entries.add(new Entry(filter));
    }

    public void remove(int i)
    {
        entries.remove(i);
    }

    public int size()
    {
        return entries.size();
    }

    public boolean isEmpty()
    {
        return entries.isEmpty();
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeInt(maxEntries);
        buf.writeNbt(writeNbt(new CompoundTag()));
    }

    public static FilterList read(FriendlyByteBuf buf)
    {
        int maxEntries = buf.readInt();
        FilterList filterList = new FilterList(maxEntries, () -> {});
        CompoundTag nbt = buf.readNbt();

        // Null check probably unnecessary
        if (nbt != null)
            filterList.readNbt(nbt);

        return filterList;
    }

    public List<Entry> getEntries()
    {
        return entries;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        ListTag list = new ListTag();
        for (Entry entry : entries)
        {
            CompoundTag entryNbt = new CompoundTag();
            entryNbt.putString("id", Filter.REGISTRY.getKey(entry.filter.getType()).toString());
            entryNbt.putBoolean("entry_inverted", entry.getInverted());
            list.add(entry.filter.writeNbt(entryNbt));
        }
        nbt.put("entries", list);
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        entries.clear();
        ListTag list = nbt.getList("entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i)
        {
            CompoundTag entryNbt = list.getCompound(i);
            ResourceLocation id = ResourceLocation.tryParse(entryNbt.getString("id"));
            Filter.Constructor<?> constructor = Filter.REGISTRY.get(id);
            if (constructor != null)
            {
                Entry entry = new Entry(constructor.create());
                entry.setInverted(entryNbt.getBoolean("entry_inverted"));
                entry.update(entryNbt);
                entries.add(entry);

            }
        }
    }

    public Filter getFilter(int i)
    {
        return entries.get(i).filter;
    }

    public void markDirty()
    {
        this.markDirty.run();
    }

    public static class Entry
    {
        private final Filter filter;
        private boolean inverted = false;

        public Entry(Filter filter)
        {
            this.filter = filter;
        }

        public boolean matches(ItemVariant variant)
        {
            return filter.matches(variant) == !inverted;
        }

        public void update(CompoundTag nbt)
        {
            filter.readNbt(nbt);
        }

        public void setInverted(boolean inverted)
        {
            this.inverted = inverted;
        }

        public Filter getFilter()
        {
            return filter;
        }

        public boolean getInverted()
        {
            return inverted;
        }
    }
}
