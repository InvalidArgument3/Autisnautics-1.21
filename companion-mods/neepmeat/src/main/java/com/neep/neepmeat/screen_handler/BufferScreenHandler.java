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

package com.neep.neepmeat.screen_handler;

import com.neep.neepmeat.init.NMScreenHandlers;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BufferScreenHandler extends AbstractContainerMenu
{
    private final Container inventory;

    public BufferScreenHandler(int syncId, Inventory playerInventory)
    {
        this(syncId, playerInventory, new SimpleContainer(9));
    }

    public BufferScreenHandler(int syncId, Inventory playerInventory, Container inventory)
    {
        super(NMScreenHandlers.BUFFER_SCREEN_HANDLER, syncId);
        checkContainerSize(inventory, 9);
        this.inventory = inventory;
        //some inventories do custom logic when a player opens it.
        inventory.startOpen(playerInventory.player);

        int m;
        int l;
        //Our inventory
        for (m = 0; m < 3; ++m)
        {
            for (l = 0; l < 3; ++l)
            {
                this.addSlot(new Slot(inventory, l + m * 3, 62 + l * 18, 17 + m * 18));
            }
        }
        //The player inventory
        for (m = 0; m < 3; ++m)
        {
            for (l = 0; l < 9; ++l)
            {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
        }
        //The player Hotbar
        for (m = 0; m < 9; ++m)
        {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
        }

    }

    @Override
    public boolean stillValid(Player player)
    {
        return this.inventory.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot)
    {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasItem())
        {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.getContainerSize())
            {
                if (!this.moveItemStackTo(originalStack, this.inventory.getContainerSize(), this.slots.size(), true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.moveItemStackTo(originalStack, 0, this.inventory.getContainerSize(), false))
            {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty())
            {
                slot.setByPlayer(ItemStack.EMPTY);
            }
            else
            {
                slot.setChanged();
//                System.out.println("oooooooo");
            }
        }

        return newStack;
    }
}

