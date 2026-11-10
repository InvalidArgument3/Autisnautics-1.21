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

package com.neep.neepmeat.transport.screen.widget;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.ParamCodecs;
import com.neep.meatlib.network.ChannelManager;
import com.neep.meatlib.screen.WidgetHandler;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.block.item_transport.RoutingNetworkImpl;
import com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class RequestListWidgetHandler implements WidgetHandler
{
    private final PipeDriverBlockEntity be;

    public final ChannelManager<SyncEntries> syncEntriesS2C;
    public final ChannelManager<Consumer<List<BlockPos>>> removeEntriesFromC2S;

    private boolean dirty = true;

    public RequestListWidgetHandler(PipeDriverBlockEntity be, Player player)
    {
        this.be = be;
        syncEntriesS2C = ChannelManager.create(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "pipe_driver/sync_requests"),
                ChannelFormat.builder(SyncEntries.class).param(ParamCodec.list(RoutingNetwork.DeferredRequest.PARAM_CODEC)).build(), player);
        removeEntriesFromC2S = ChannelManager.create(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "pipe_driver/remove_entries"),
                ChannelFormat.builder(ParamCodecs.<Consumer<List<BlockPos>>>as(Consumer.class)).param(ParamCodecs.BLOCK_POS.listOf()).build(), player);

        if (!MeatLib.isClient())
        {
            removeEntriesFromC2S.receiver(this::receiveRemoveEntry);
        }
    }

    private void receiveRemoveEntry(List<BlockPos> positions)
    {
        if (be.getNetwork(null) instanceof RoutingNetworkImpl impl)
        {
            impl.removeDeferredFrom(positions);
            dirty = true;
        }
    }

    @Override
    public void sendUpdates()
    {
        if (dirty || be.getLevel().getGameTime() % 5 == 0)
        {
            if (be.getNetwork(null) instanceof RoutingNetworkImpl impl)
                syncEntriesS2C.emitter().apply(impl.getActiveRequests());

            dirty = false;
        }
    }

    @Override
    public void close()
    {
        syncEntriesS2C.close();
        removeEntriesFromC2S.close();
    }

    public interface SyncEntries
    {
        void apply(List<RoutingNetwork.DeferredRequest> entries);
    }
}
