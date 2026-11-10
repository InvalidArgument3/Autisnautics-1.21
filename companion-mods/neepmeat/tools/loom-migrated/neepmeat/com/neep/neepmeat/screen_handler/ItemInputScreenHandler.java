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
import net.minecraft.world.inventory.Slot;

public class ItemInputScreenHandler extends BasicScreenHandler
{
    public ItemInputScreenHandler(Inventory playerInventory, Container inventory, int syncId)
    {
        super(NMScreenHandlers.ITEM_INPUT, playerInventory, inventory, syncId, null);
        createSlotBlock(0, 0, 3, 3, inventory, 0, Slot::new);
        createPlayerSlots(0, 0, playerInventory);
    }

    public ItemInputScreenHandler(int syncId, Inventory playerInventory)
    {
        this(playerInventory, new SimpleContainer(9), syncId);
    }
}
