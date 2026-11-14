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

package com.neep.neepmeat.inventory;

import java.util.Arrays;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CombinedInventory implements Container
{
    protected Container[] inventories;
    protected int[] sizes;

    public CombinedInventory(Container... inventories)
    {
        this.inventories = inventories;
        this.sizes = new int[inventories.length];
        for (int i = 0; i < inventories.length; ++i)
        {
            sizes[i] = inventories[i].getContainerSize();
        }
    }

    public int wrapSlot(int slot)
    {
        int total = 0;
        for (int size : sizes)
        {
            if (slot >= total && slot < total + size)
                return slot - total;
            total += size;
        }
        return -1;
    }

    public Container getInventory(int slot)
    {
        int total = 0;
        // Return if index is within the sub-inventory's range of indices
        for (Container inventory : inventories)
        {
            int size = inventory.getContainerSize();
            if (slot >= total && slot < total + size)
                return inventory;
            total += size;
        }
        return null;
    }

    @Override
    public int getContainerSize()
    {
        return Arrays.stream(sizes).sum();
    }

    @Override
    public boolean isEmpty()
    {
        return Arrays.stream(inventories).allMatch(Container::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot)
    {
        return getInventory(slot).getItem(wrapSlot(slot));
    }

    @Override
    public ItemStack removeItem(int slot, int amount)
    {
        return getInventory(slot).removeItem(wrapSlot(slot), amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot)
    {
        return getInventory(slot).removeItemNoUpdate(wrapSlot(slot));
    }

    @Override
    public void setItem(int slot, ItemStack stack)
    {
        getInventory(slot).setItem(wrapSlot(slot), stack);
    }

    @Override
    public void setChanged()
    {

    }

    @Override
    public boolean stillValid(Player player)
    {
        return true;
    }

    @Override
    public void clearContent()
    {
        Arrays.stream(inventories).forEach(Container::clearContent);
    }
}
