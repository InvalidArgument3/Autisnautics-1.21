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

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.network.ChannelManager;
import com.neep.neepbus.screen.NeepBusConfigScreenHandler;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.item.filter.FilterList;
import com.neep.neepmeat.screen_handler.BasicScreenHandler;
import com.neep.neepmeat.transport.block.item_transport.entity.RequesterBlockEntity;
import com.neep.neepmeat.transport.screen.widget.RequesterConfigWidgetHandler;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class RequesterScreenHandler extends BasicScreenHandler
{
    public final FilterWidgetHandler filterHandler;
    public final RequesterConfigWidgetHandler requesterConfigHandler;
    public final List<Slot> playerSlots;

    public final ChannelManager<Runnable> openNeepBusC2S;
    private final RequesterBlockEntity be;

    public RequesterScreenHandler(Inventory playerInventory, int syncId, RequesterBlockEntity be, FilterList filter)
    {
        super(TransportScreenHandlers.REQUESTER, playerInventory, syncId);
        this.be = be;
        this.filterHandler = addWidgetHandler(new FilterWidgetHandler(playerInventory.player, filter));
        this.requesterConfigHandler = addWidgetHandler(new RequesterConfigWidgetHandler(be, playerInventory.player));

        this.playerSlots = createPlayerSlots(0, 0, playerInventory);
        openNeepBusC2S = addClosable(ChannelManager.create(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "requester/open_neepbus"),
                ChannelFormat.RUNNABLE, playerInventory.player));

        if (!MeatLib.isClient())
        {
            openNeepBusC2S.receiver(this::openNeepBus);
        }
    }

    private void openNeepBus()
    {
        playerInventory.player.openMenu(NeepBusConfigScreenHandler.getFactory(be.getConfig()));
    }

    public RequesterScreenHandler(int syncId, Inventory playerInventory)
    {
        this(playerInventory, syncId, null, new FilterList(1, () -> {}));
    }
}
