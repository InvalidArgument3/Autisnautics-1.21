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
import com.neep.neepbus.util.NeepBusConfig;
import com.neep.neepbus.part.Ranged;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class NeepBusRangeConfigScreenHandler extends NeepBusConfigScreenHandler
{
    public final RangeConfigHandler rangeConfig;

    public static ExtendedScreenHandlerFactory getFactory(NeepBusConfig config, Ranged ranged)
    {
        return new ExtendedScreenHandlerFactory()
        {
            @Override
            public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf)
            {
                NeepBusConfigScreenHandler.writeOpeningData(config, buf);
            }

            @Override
            public Component getDisplayName()
            {
                return Component.empty();
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player)
            {
                return new NeepBusRangeConfigScreenHandler(playerInventory, syncId, config, ranged);
            }
        };
    }

    public NeepBusRangeConfigScreenHandler(MenuType<?> type, Inventory playerInventory, int syncId, NeepBusConfig config, Ranged ranged)
    {
        super(type, playerInventory, syncId, config);
        rangeConfig = new RangeConfigHandler(playerInventory.player, ranged);
    }

    public NeepBusRangeConfigScreenHandler(Inventory playerInventory, int syncId, NeepBusConfig config, Ranged ranged)
    {
        this(NeepBusScreenHandlers.NEEPBUS_RANGE_CONFIG, playerInventory, syncId, config, ranged);
    }

    public NeepBusRangeConfigScreenHandler(int syncId, Inventory player, FriendlyByteBuf buf)
    {
        super(syncId, player, buf);
        rangeConfig = new RangeConfigHandler(playerInventory.player, Ranged.EMPTY);
    }

    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();

        if (ready)
            rangeConfig.sendUpdates();
    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);
        rangeConfig.close();
    }
}
