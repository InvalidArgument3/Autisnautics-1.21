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

package com.neep.neepmeat.machine.separator;

import com.neep.neepmeat.init.NMScreenHandlers;
import com.neep.neepmeat.screen_handler.BasicScreenHandler;
import net.minecraft.world.entity.player.Inventory;

public class SeparatorScreenHandler extends BasicScreenHandler
{
    public final SeparatorWidgetHandler handler;

    public SeparatorScreenHandler(int syncId, Inventory playerInventory)
    {
        this(playerInventory, syncId, null);
    }

    public SeparatorScreenHandler(Inventory playerInventory, int syncId, SeparatorBlockEntity be)
    {
        super(NMScreenHandlers.SEPARATOR, playerInventory, null, syncId, null);
        this.handler = addWidgetHandler(new SeparatorWidgetHandler(playerInventory.player, be));
    }
}
