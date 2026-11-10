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

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.network.ChannelManager;
import com.neep.neepbus.screen.NeepBusConfigScreenHandler;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMScreenHandlers;
import com.neep.neepmeat.screen_handler.widget.VascularCondenserWidgetHandler;
import com.neep.neepmeat.transport.block.energy_transport.entity.VascularCondenserBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class VascularCondenserScreenHandler extends BasicScreenHandler
{
    public final VascularCondenserWidgetHandler widgetHandler;
    public final ChannelManager<Runnable> openNeepbus;

    public VascularCondenserScreenHandler(int syncId, Inventory playerInventory)
    {
        this(playerInventory, syncId, null);
    }

    public VascularCondenserScreenHandler(Inventory playerInventory, int syncId, VascularCondenserBlockEntity be)
    {
        super(NMScreenHandlers.VASCULAR_CONDENSER, playerInventory, null, syncId, null);

        this.widgetHandler = addWidgetHandler(new VascularCondenserWidgetHandler(playerInventory.player, be));

        this.openNeepbus = ChannelManager.create(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "vascular_condenser/open_neepbus"),
                ChannelFormat.RUNNABLE, playerInventory.player);

        if (!MeatLib.isClient())
        {
            openNeepbus.receiver(() ->
            {
                if (be != null)
                    playerInventory.player.openMenu(NeepBusConfigScreenHandler.getFactory(be.getConfig()));
            });
        }
    }
}
