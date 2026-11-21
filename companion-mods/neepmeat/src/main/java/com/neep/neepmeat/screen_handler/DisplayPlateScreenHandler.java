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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public class DisplayPlateScreenHandler extends BasicScreenHandler
{
    private final int initialCapacity;

    public DisplayPlateScreenHandler(int syncId, Inventory inventory, FriendlyByteBuf byteBuf)
    {
        this(inventory, syncId, new SimpleContainerData(1), byteBuf.readInt());
    }

    public DisplayPlateScreenHandler(Inventory playerInventory, int syncId, ContainerData delegate, int initialCapacity)
    {
        super(NMScreenHandlers.DISPLAY_PLATE, playerInventory, null, syncId, delegate);
        this.initialCapacity = initialCapacity;
    }

    public int getCapacity()
    {
        return initialCapacity;
    }

    public void setCapacity(int capacity)
    {
        ScreenPropertyC2SPacket.Client.send(0, capacity);
    }
}
