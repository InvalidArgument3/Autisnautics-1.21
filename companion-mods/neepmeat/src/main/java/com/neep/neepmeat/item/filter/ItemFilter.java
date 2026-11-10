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

import com.neep.meatlib.transfer.ItemVariant;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import java.util.List;
import java.util.Objects;

public class ItemFilter implements Filter
{
    private final List<ItemVariant> items = NonNullList.withSize(10, ItemVariant.BLANK);
    private boolean useNbt = false;
    private boolean useDamage = false;

    public ItemFilter()
    {

    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        ListTag itemList = new ListTag();
        for (var item : items)
        {
            itemList.add(item.toNbt());
        }
        nbt.put("items", itemList);

        nbt.putBoolean("use_nbt", useNbt);
        nbt.putBoolean("use_damage", useDamage);

        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        ListTag itemList = nbt.getList("items", Tag.TAG_COMPOUND);
        for (int i = 0; i < itemList.size(); ++i)
        {
            items.set(i, ItemVariant.fromNbt(itemList.getCompound(i)));
        }

        this.useNbt = nbt.getBoolean("use_nbt");
        this.useDamage = nbt.getBoolean("use_damage");
    }

    public List<ItemVariant> getItems()
    {
        return items;
    }

    public void setItem(int index, ItemVariant item)
    {
        if (index < 0 || index >= items.size())
            return;

        items.set(index, item);
    }

    public ItemVariant getItem(int index)
    {
        if (index < 0 || index >= items.size())
            return ItemVariant.BLANK;

        return items.get(index);
    }

    @Override
    public boolean matches(ItemVariant variant)
    {
        for (var item : items)
        {
            if (testItem(item, variant))
                return true;
        }
        return false;
    }

    private boolean testItem(ItemVariant filterItem, ItemVariant variant)
    {
        boolean itemsEqual = variant.getItem() == filterItem.getItem();
        if (!useNbt && !useDamage)
            return itemsEqual;

        CompoundTag filterNbt = filterItem.getNbt();
        CompoundTag nbt = variant.getNbt();
        int filterDamage = filterNbt == null ? 0 : filterNbt.getInt("Damage");
        int itemDamage = nbt == null ? 0 : nbt.getInt("Damage");
        if (useDamage && !useNbt)
        {
            return itemsEqual && filterDamage == itemDamage;
        }
        else
        {
            return itemsEqual && Objects.equals(filterNbt, nbt);
        }
    }

    @Override
    public Constructor<?> getType()
    {
        return Filters.ITEM;
    }

    public boolean ignoreDamage()
    {
        return useDamage;
    }

    public boolean ignoreNbt()
    {
        return useNbt;
    }

    public void setUseDamage(boolean useDamage)
    {
        this.useDamage = useDamage;
        if (!useDamage)
            this.useNbt = false;
    }

    public void setUseNbt(boolean useNbt)
    {
        this.useNbt = useNbt;
        if (useNbt)
            this.useDamage = true;
    }
}
