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

import com.neep.meatlib.screen.ScreenReadyListener;
import com.neep.neepbus.NeepBusScreenHandlers;
import com.neep.neepbus.util.NeepBusConfig;
import com.neep.neepmeat.screen_handler.BasicScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import java.util.List;

public class NeepBusConfigScreenHandler extends BasicScreenHandler implements ScreenReadyListener
{
    public final AddressConfigHandler configHandler;

    public static ExtendedScreenHandlerFactory getFactory(NeepBusConfig config)
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
                return new NeepBusConfigScreenHandler(playerInventory, syncId, config);
            }
        };
    }

    public static void writeOpeningData(NeepBusConfig config, FriendlyByteBuf buf)
    {
        AddressConfigHandler.SYNC_ENTRY_CODEC.encode(config.getInputs().stream().map(AddressConfigHandler.SyncEntry::of).toList(), buf);
        AddressConfigHandler.SYNC_ENTRY_CODEC.encode(config.getOutputs().stream().map(AddressConfigHandler.SyncEntry::of).toList(), buf);
    }

    // For extension
    protected NeepBusConfigScreenHandler(MenuType<?> type, Inventory playerInventory, int syncId, NeepBusConfig config)
    {
        super(type, playerInventory, null, syncId, null);

        this.configHandler = new AddressConfigHandler(config, playerInventory.player);
    }

    public NeepBusConfigScreenHandler(int syncId, Inventory playerInventory, FriendlyByteBuf buf)
    {
        this(playerInventory, syncId, null);
        configHandler.receiveOpeningData(buf);
    }

    public NeepBusConfigScreenHandler(Inventory playerInventory, int syncId, NeepBusConfig config)
    {
        this(NeepBusScreenHandlers.NEEPBUS_CONFIG, playerInventory, syncId, config);

    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);

        configHandler.close();
    }

    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();

        if (ready)
        {
            configHandler.sendUpdates();
        }
    }

    @FunctionalInterface
    public interface OnAddressChange
    {
        void onAddressChange(boolean output, int idx, String newAddress);
    }

    @FunctionalInterface
    public interface UpdateEntries
    {
        void update(List<AddressConfigHandler.SyncEntry> entries);
    }
}
