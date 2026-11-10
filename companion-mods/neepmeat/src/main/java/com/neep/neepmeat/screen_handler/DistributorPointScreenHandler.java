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

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.network.ChannelManager;
import com.neep.neepbus.screen.NeepBusConfigScreenHandler;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMScreenHandlers;
import com.neep.neepmeat.machine.live_machine.block.entity.DistributorPointBlockEntity;
import com.neep.neepmeat.screen_handler.widget.DistributorCooldownWidgetHandler;
import com.neep.neepmeat.screen_handler.widget.DistributorPointWidgetHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class DistributorPointScreenHandler extends BasicScreenHandler
{
    public final DistributorPointWidgetHandler handler;
    public final DistributorCooldownWidgetHandler cooldownHandler;
    public final ChannelManager<Runnable> openConfigC2S;
    @Nullable private final DistributorPointBlockEntity be;

    public DistributorPointScreenHandler(Inventory playerInventory, int syncId, @Nullable DistributorPointBlockEntity be)
    {
        super(NMScreenHandlers.DISTRIBUTOR_POINT, playerInventory, null, syncId, null);
        this.be = be;
        this.handler = addWidgetHandler(new DistributorPointWidgetHandler(playerInventory.player, be));
        this.cooldownHandler = addWidgetHandler(new DistributorCooldownWidgetHandler(playerInventory.player, be));
        openConfigC2S = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "distributor_open_neepbus"),
                ChannelFormat.RUNNABLE, playerInventory.player);

        if (!playerInventory.player.level().isClientSide())
        {
            openConfigC2S.receiver(this::openConfig);
        }
    }

    public DistributorPointScreenHandler(int syncId, Inventory playerInventory)
    {
        this(playerInventory, syncId, null);
    }

    private void openConfig()
    {
        playerInventory.player.openMenu(NeepBusConfigScreenHandler.getFactory(be.getConfig()));
    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);
        openConfigC2S.close();
    }
}
