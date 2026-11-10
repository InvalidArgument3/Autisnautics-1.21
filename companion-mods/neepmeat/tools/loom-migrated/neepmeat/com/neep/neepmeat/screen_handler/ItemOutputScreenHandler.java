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
import com.neep.neepmeat.network.ScreenPropertyC2SPacket;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;

public class ItemOutputScreenHandler extends BasicScreenHandler
{
    public ItemOutputScreenHandler(Inventory playerInventory, Container inventory, int syncId, ContainerData propertyDelegate)
    {
        super(NMScreenHandlers.ITEM_OUTPUT, playerInventory, inventory, syncId, propertyDelegate);

        createSlotBlock(18, 16, 3, 3, inventory, 0, Slot::new);

        createPlayerSlots(8, 96, playerInventory);
    }

    public ItemOutputScreenHandler(int syncId, Inventory playerInventory)
    {
        this(playerInventory, new SimpleContainer(9), syncId, new SimpleContainerData(1));
    }

    public void setEject(boolean eject)
    {
        ScreenPropertyC2SPacket.Client.send(0, eject ? 1 : 0);
    }
}
