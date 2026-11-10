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
import com.neep.neepbus.block.entity.PIDController;
import com.neep.neepbus.util.NeepBusConfig;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class PIDControllerScreenHandler extends NeepBusConfigScreenHandler
{
    public final PIDConfigHandler pidHandler;
    private final PIDController pid;

    public static ExtendedScreenHandlerFactory getFactory(NeepBusConfig config, PIDController pid)
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
                return new PIDControllerScreenHandler(syncId, playerInventory, pid, config);
            }
        };
    }

    public PIDControllerScreenHandler(int syncId, Inventory playerInventory, FriendlyByteBuf buf)
    {
        super(syncId, playerInventory, buf);
        this.pid = PIDController.EMPTY;
        pidHandler = new PIDConfigHandler(playerInventory.player, pid);
    }

    public PIDControllerScreenHandler(int syncId, Inventory playerInventory, PIDController pid, NeepBusConfig config)
    {
        super(NeepBusScreenHandlers.PID_CONTROLLER, playerInventory, syncId, config);
        this.pid = pid;
        pidHandler = new PIDConfigHandler(playerInventory.player, pid);
    }

    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();
        if (ready)
        {
            pidHandler.sendUpdates();
        }
    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);
        pidHandler.close();
    }
}
