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
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class StirlingEngineScreenHandler extends AbstractContainerMenu
{
    private final Container inventory;
    private final ContainerData propertyDelegate;

    // Client
    public StirlingEngineScreenHandler(int syncId, Inventory playerInventory)
    {
        this(syncId, playerInventory, new SimpleContainer(1), new SimpleContainerData(4));
    }

    // Server
    public StirlingEngineScreenHandler(int syncId, Inventory playerInventory, Container inventory, ContainerData delegate)
    {
        super(NMScreenHandlers.STIRLING_ENGINE, syncId);
        checkContainerSize(inventory, 1);
        this.inventory = inventory;
        this.propertyDelegate = delegate;

        inventory.startOpen(playerInventory.player);

        this.addDataSlots(delegate);

        createSlots();
        createPlayerSlots(playerInventory);
    }

    public int getProperty(int i)
    {
        return propertyDelegate.get(i);
    }

    @Override
    public boolean stillValid(Player player)
    {
        return this.inventory.stillValid(player);
    }

    protected void createSlots()
    {
        this.addSlot(new Slot(inventory, 0, 80, 54));
    }

    public void createPlayerSlots(Inventory playerInventory)
    {
        int m;
        int l;
        for (m = 0; m < 3; ++m)
        {
            for (l = 0; l < 9; ++l)
            {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 89 + m * 18));
            }
        }

        for (m = 0; m < 9; ++m)
        {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 147));
        }
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
            }
        }

        return newStack;
    }
}
