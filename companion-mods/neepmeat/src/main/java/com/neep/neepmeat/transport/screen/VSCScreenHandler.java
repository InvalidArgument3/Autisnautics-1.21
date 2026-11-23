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

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.network.ChannelManager;
import com.neep.neepbus.screen.NeepBusConfigScreenHandler;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.screen_handler.BasicScreenHandler;
import com.neep.neepmeat.transport.block.energy_transport.entity.vsc.VSCBlockEntity;
import com.neep.neepmeat.transport.screen.widget.VSCConfigWidgetHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

public class VSCScreenHandler extends BasicScreenHandler
{
    public final VSCConfigWidgetHandler widgetHandler;
    public final ChannelManager<Runnable> openConfigC2S;

    public VSCScreenHandler(int syncId, Inventory playerInventory)
    {
        this(playerInventory, syncId, null);
    }

    public VSCScreenHandler(Inventory playerInventory, int syncId, @Nullable VSCBlockEntity be)
    {
        super(TransportScreenHandlers.VSC, playerInventory, null, syncId, null);

        this.widgetHandler = addWidgetHandler(new VSCConfigWidgetHandler(be, playerInventory.player));
        this.openConfigC2S = addClosable(ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "vsc/open_neepbus"), ChannelFormat.RUNNABLE, playerInventory.player));

        if (!playerInventory.player.level().isClientSide())
        {
            openConfigC2S.receiver(() ->
            {
                if (be != null)
                    playerInventory.player.openMenu(NeepBusConfigScreenHandler.getFactory(be.getConfig()));
            });
        }
    }
}
