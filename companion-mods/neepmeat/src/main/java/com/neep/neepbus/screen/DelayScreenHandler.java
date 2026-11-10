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

package com.neep.neepbus.screen;

import com.neep.neepbus.NeepBusScreenHandlers;
import com.neep.neepbus.block.entity.Delay;
import com.neep.neepbus.util.NeepBusConfig;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class DelayScreenHandler extends NeepBusConfigScreenHandler
{
    public final DelayConfigHandler delayHandler;

    public static ExtendedScreenHandlerFactory getFactory(NeepBusConfig config, Delay ranged)
    {
        return new ExtendedScreenHandlerFactory()
        {
            public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf)
            {
                NeepBusConfigScreenHandler.writeOpeningData(config, buf);
            }

            public Component getDisplayName()
            {
                return Component.empty();
            }

            public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player)
            {
                return new DelayScreenHandler(playerInventory, syncId, config, ranged);
            }
        };
    }

    public DelayScreenHandler(Inventory playerInventory, int syncId, NeepBusConfig config, Delay delay)
    {
        super(NeepBusScreenHandlers.DELAY, playerInventory, syncId, config);
        this.delayHandler = addWidgetHandler(new DelayConfigHandler(playerInventory.player, delay));
    }

    public DelayScreenHandler(int syncId, Inventory playerInventory, FriendlyByteBuf buf)
    {
        super(syncId, playerInventory, buf);
        this.delayHandler = addWidgetHandler(new DelayConfigHandler(playerInventory.player, Delay.EMPTY));
    }
}
