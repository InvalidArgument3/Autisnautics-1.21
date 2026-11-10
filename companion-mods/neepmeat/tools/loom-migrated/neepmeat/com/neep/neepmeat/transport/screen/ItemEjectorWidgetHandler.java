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

import com.neep.meatlib.network.ChannelManager;
import com.neep.meatlib.screen.WidgetHandler;
import com.neep.meatlib.util.RedstoneMode;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.transport.block.item_transport.machine.ItemEjector;
import com.neep.neepmeat.transport.block.item_transport.machine.ItemEjectorUpdate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ItemEjectorWidgetHandler implements WidgetHandler
{
    public final ChannelManager<ItemEjectorUpdate> updateC2SS2C;

    private final ItemEjector itemEjector;
    protected boolean updateToClient = true;

    public ItemEjectorWidgetHandler(ItemEjector itemEjector, Player player)
    {
        this.itemEjector = itemEjector;

        updateC2SS2C = ChannelManager.create(new ResourceLocation(NeepMeat.NAMESPACE, "transport/advanced_ejector_update"),
                        ItemEjectorUpdate.CHANNEL_FORMAT, player);

        if (!player.level().isClientSide())
            updateC2SS2C.receiver(this::receiveFromClient);
    }

    private void receiveFromClient(boolean roundRobin, int amount, int period, RedstoneMode redstoneMode)
    {
        itemEjector.update(roundRobin, amount, period, redstoneMode);
        updateToClient = true;
    }

    @Override
    public void sendUpdates()
    {
        if (updateToClient)
        {
            updateC2SS2C.emitter().update(
                    itemEjector.roundRobin(), itemEjector.amount(), itemEjector.period(), itemEjector.redstoneMode());

            updateToClient = false;
        }
    }

    @Override
    public void close()
    {
        updateC2SS2C.close();
    }
}
