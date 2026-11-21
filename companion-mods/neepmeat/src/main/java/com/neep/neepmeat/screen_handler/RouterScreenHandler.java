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
import com.neep.neepmeat.screen_handler.slot.PatternSlot;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public class RouterScreenHandler extends BasicScreenHandler
{
    // Client
    public RouterScreenHandler(int syncId, Inventory playerInventory)
    {
        this(syncId, playerInventory, new SimpleContainer(18));
    }

    // Server
    public RouterScreenHandler(int syncId, Inventory playerInventory, Container inventory)
    {
        super(NMScreenHandlers.ROUTER, playerInventory, inventory, syncId, null);
        checkContainerSize(inventory, 18);

        createSlots();
        createPlayerSlots(8, 95, playerInventory);
    }

    protected void createSlots()
    {
        int m, l;
        for (m = 0; m < 6; ++m)
        {
            for (l = 0; l < 3; ++l)
            {
                this.addSlot(new PatternSlot(inventory, l + m * 3, 13 + m * (18 + 9), 19 + l * 18));
            }
        }
    }

    @Override
    public boolean stillValid(Player player)
    {
        return this.inventory.stillValid(player);
    }

    public void clicked(int slotIndex, int button, ClickType actionType, Player player)
    {
        super.clicked(slotIndex, button, actionType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        return ItemStack.EMPTY;
    }
}

