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

package com.neep.neepmeat.transport.block.item_transport.machine;

import com.neep.meatlib.inventory.ImplementedInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public class RouterInventory implements ImplementedInventory
{
    protected NonNullList<ItemStack> items = NonNullList.withSize(18, ItemStack.EMPTY);

    @Override
    public NonNullList<ItemStack> getItems()
    {
        return items;
    }

    @Override
    public ItemStack removeItem(int slot, int count)
    {
        return ImplementedInventory.super.removeItem(slot, count);
    }
}
