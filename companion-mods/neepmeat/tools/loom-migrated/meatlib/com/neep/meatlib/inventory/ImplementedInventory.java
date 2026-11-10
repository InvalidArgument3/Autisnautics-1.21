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

package com.neep.meatlib.inventory;

import com.neep.meatlib.util.NbtSerialisable;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ImplementedInventory extends Container, NbtSerialisable
{
    Container EMPTY = ofSize(0);

    static ImplementedInventory of(NonNullList<ItemStack> items)
    {
        return () -> items;
    }

    static ImplementedInventory of(NonNullList<ItemStack> items, Runnable callback)
    {
        return new ImplementedInventory()
        {
            @Override
            public NonNullList<ItemStack> getItems()
            {
                return items;
            }

            @Override
            public void setChanged()
            {
                callback.run();
            }
        };
    }

    static ImplementedInventory ofSize(int size)
    {
        return of(NonNullList.withSize(size, ItemStack.EMPTY));
    }

    static ImplementedInventory ofSize(int size, Runnable callback)
    {
        return of(NonNullList.withSize(size, ItemStack.EMPTY), callback);
    }

    static void readNbt(CompoundTag nbt, NonNullList<ItemStack> stacks)
    {
        ListTag nbtList = nbt.getList("Items", Tag.TAG_COMPOUND);

        // Inventories::readNbt does nothing if the received list is empty.
        if (nbtList.isEmpty())
        {
            stacks.clear();
            return;
        }

        for (int i = 0; i < nbtList.size(); ++i)
        {
            CompoundTag nbtCompound = nbtList.getCompound(i);
            int j = nbtCompound.getByte("Slot") & 255;
            if (j >= 0 && j < stacks.size())
            {
                stacks.set(j, ItemStack.of(nbtCompound));
            }
        }
    }

    NonNullList<ItemStack> getItems();

    @Override
    default int getContainerSize()
    {
        return getItems().size();
    }

    @Override
    default boolean isEmpty()
    {
        for (int i = 0; i < getContainerSize(); i++)
        {
            ItemStack stack = getItem(i);
            if (!stack.isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    default float emptyStacks()
    {
        int filled = 0;
        for (int i = 0; i < getContainerSize(); i++)
        {
            ItemStack stack = getItem(i);
            if (!stack.isEmpty())
            {
                filled++;
            }
        }
        return getContainerSize() - filled;
    }

    @Override
    default ItemStack getItem(int slot)
    {
        return getItems().get(slot);
    }

    @Override
    default ItemStack removeItem(int slot, int count)
    {
        ItemStack result = ContainerHelper.removeItem(getItems(), slot, count);
        if (!result.isEmpty())
        {
            setChanged();
        }
        return result;
    }

    @Override
    default ItemStack removeItemNoUpdate(int slot)
    {
        return removeItem(slot, getItems().get(slot).getCount());
    }

    @Override
    default void setItem(int slot, ItemStack stack)
    {
        getItems().set(slot, stack);
    }

    @Override
    default void clearContent()
    {
        getItems().clear();
    }

    @Override
    default void setChanged()
    {
    }

    @Override
    default boolean stillValid(Player player)
    {
        return true;
    }

    default CompoundTag writeNbt(CompoundTag nbt)
    {
        ContainerHelper.saveAllItems(nbt, getItems());
        return nbt;
    }

    default void readNbt(CompoundTag tag)
    {
        readNbt(tag, getItems());
    }
}
