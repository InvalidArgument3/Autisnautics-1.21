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

package com.neep.neepmeat.transport.screen;

import com.neep.meatlib.screen.ScreenReadyListener;
import com.neep.meatlib.screen.SimpleScreenHandlerFactory;
import com.neep.neepmeat.screen_handler.BasicScreenHandler;
import com.neep.neepmeat.transport.api.item_network.PriorityPipe;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class PipePriorityScreenHandler extends BasicScreenHandler implements ScreenReadyListener
{
    public PipePriorityWidgetHandler priorityHandler;
    private boolean ready;

    public static MenuProvider open(PriorityPipe priorityPipe)
    {
        return ((SimpleScreenHandlerFactory) (s, p, pl) -> new PipePriorityScreenHandler(p, s, priorityPipe));
    }

    public PipePriorityScreenHandler(int syncId, Inventory playerInventory)
    {
        this(playerInventory, syncId, PriorityPipe.EMPTY);
    }

    protected PipePriorityScreenHandler(Inventory playerInventory, int syncId, PriorityPipe priorityPipe)
    {
        super(TransportScreenHandlers.PIPE_PRIORITY, playerInventory, null, syncId, null);
        this.priorityHandler = new PipePriorityWidgetHandler(playerInventory.player, priorityPipe);
    }

    @Override
    public void removed(Player player)
    {
        priorityHandler.close();
        super.removed(player);
    }

    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();
        if (ready)
        {
            priorityHandler.sendUpdates();
        }
    }

    @Override
    public void onScreenReady()
    {
        ready = true;
    }
}
