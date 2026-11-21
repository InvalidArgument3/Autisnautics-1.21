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

import com.neep.neepmeat.item.filter.FilterList;
import com.neep.neepmeat.screen_handler.BasicScreenHandler;
import com.neep.neepmeat.transport.block.item_transport.machine.ItemEjector;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedEjectorScreenHandler extends BasicScreenHandler
{
    public final FilterWidgetHandler filterHandler;
    public final ItemEjectorWidgetHandler ejectorHandler;

    public AdvancedEjectorScreenHandler(int syncId, Inventory playerInventory)
    {
        this(new FilterList(0, () -> {}), ItemEjector.EMPTY, playerInventory, syncId);
    }

    public AdvancedEjectorScreenHandler(FilterList filterList, ItemEjector itemEjector, Inventory playerInventory, int syncId)
    {
        super(TransportScreenHandlers.ADVANCED_EJECTOR, playerInventory, null, syncId, null);
        this.filterHandler = addWidgetHandler(new FilterWidgetHandler(playerInventory.player, filterList));
        this.ejectorHandler = addWidgetHandler(new ItemEjectorWidgetHandler(itemEjector, playerInventory.player));

        createInventory(0, 0, playerInventory);
        createHotbar(0, 0, playerInventory);
    }
}
