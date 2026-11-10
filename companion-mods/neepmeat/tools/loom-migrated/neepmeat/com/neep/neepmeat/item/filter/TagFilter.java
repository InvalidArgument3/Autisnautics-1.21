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

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TagFilter implements Filter
{
    @Nullable
    private TagKey<Item> tag;

    @Override
    public boolean matches(ItemVariant variant)
    {
        return variant.getItem().builtInRegistryHolder().tags().anyMatch(t -> Objects.equals(t, tag));
    }

    @Override
    public Constructor<?> getType()
    {
        return Filters.TAG;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        if (tag != null)
            nbt.putString("tag", tag.location().toString());
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        if (nbt.contains("tag"))
        {
            ResourceLocation id = ResourceLocation.tryParse(nbt.getString("tag"));
            if (id != null)
                tag = TagKey.create(BuiltInRegistries.ITEM.key(), id);
            else
                tag = null;
        }
        else
        {
            tag = null;
        }
    }

    public void setTag(@Nullable TagKey<Item> tag)
    {
        this.tag = tag;
    }

    @Nullable
    public TagKey<Item> getTag()
    {
        return tag;
    }
}
