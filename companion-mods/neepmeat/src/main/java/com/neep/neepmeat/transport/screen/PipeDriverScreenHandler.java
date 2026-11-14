/*
 * Copyright (c) MeatWheeze 2026.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.transport.screen;

import com.neep.neepmeat.screen_handler.BasicScreenHandler;
import com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity;
import com.neep.neepmeat.transport.screen.widget.RequestListWidgetHandler;
import net.minecraft.world.entity.player.Inventory;

public class PipeDriverScreenHandler extends BasicScreenHandler
{
    public final RequestListWidgetHandler requestList;

    public PipeDriverScreenHandler(int syncId, Inventory playerInventory)
    {
        this(playerInventory, null, syncId);
    }

    public PipeDriverScreenHandler(Inventory playerInventory, PipeDriverBlockEntity be, int syncId)
    {
        super(TransportScreenHandlers.PIPE_DRIVER, playerInventory, syncId);
        this.requestList = addWidgetHandler(new RequestListWidgetHandler(be, playerInventory.player));
    }
}
