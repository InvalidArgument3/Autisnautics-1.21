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

import com.neep.neepmeat.plc.block.entity.ProcessInterfaceBlockEntity;
import com.neep.neepmeat.screen_handler.BasicScreenHandler;
import com.neep.neepmeat.screen_handler.CardSlot;
import com.neep.neepmeat.transport.ItemTransport;
import com.neep.neepmeat.transport.screen.widget.ProcessInterfaceWidgetHandler;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;

public class ProcessInterfaceScreenHandler extends BasicScreenHandler
{
    public final List<Slot> playerSlots;
    public final List<Slot> resultSlots;
    public final List<Slot> cardSlots;

    public final ProcessInterfaceWidgetHandler widgetHandler;

    public ProcessInterfaceScreenHandler(int syncId, Inventory playerInventory)
    {
        this(playerInventory, new SimpleContainer(9), new SimpleContainer(ProcessInterfaceBlockEntity.MAX_CARDS), null, syncId);
    }

    public ProcessInterfaceScreenHandler(Inventory playerInventory, Container resultInventory, Container cardInventory, ProcessInterfaceBlockEntity be, int syncId)
    {
        super(TransportScreenHandlers.ITEM_REQUEST_LISTENER, playerInventory, cardInventory, syncId, null);

        this.cardSlots = createSlotBlock(ProcessInterfaceBlockEntity.MAX_CARDS, cardInventory, 0, ProcessCardSlot::new);
        this.playerSlots = createPlayerSlots(0, 0, playerInventory);
        this.resultSlots = createSlotBlock(0, 0, 3, 3, resultInventory, 0, Slot::new);

        this.widgetHandler = addWidgetHandler(new ProcessInterfaceWidgetHandler(playerInventory.player, be));
    }

    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();
    }

    private static class ProcessCardSlot extends CardSlot
    {
        public ProcessCardSlot(Container inventory, int index, int x, int y)
        {
            super(inventory, index, x, y);
        }

        @Override
        public Item validItem()
        {
            return ItemTransport.PROCESS_CARD;
        }
    }
}
