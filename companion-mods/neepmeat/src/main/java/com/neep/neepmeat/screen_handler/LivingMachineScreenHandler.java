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
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.ParamCodecs;
import com.neep.meatlib.network.ChannelManager;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.api.live_machine.metrics.DataLog;
import com.neep.neepmeat.init.NMScreenHandlers;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class LivingMachineScreenHandler extends BasicScreenHandler
{
    private final LivingMachineBlockEntity be;

    public final ChannelManager<SyncGraph> syncGraph;

    private boolean initialSync = true;

    public LivingMachineScreenHandler(int syncId, Inventory playerInventory, FriendlyByteBuf buf)
    {
        this(playerInventory, syncId, (LivingMachineBlockEntity) playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public LivingMachineScreenHandler(Inventory playerInventory, int syncId, LivingMachineBlockEntity be)
    {
        super(NMScreenHandlers.LIVING_MACHINE, playerInventory, null, syncId, null);
        this.be = be;
        this.syncGraph = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "living_machine/sync_graph"),
                ChannelFormat.builder(SyncGraph.class)
                        .param(DataLog.PARAM_CODEC)
                        .param(ParamCodec.list(ParamCodecs.TEXT_AS_STRING))
                        .build(), playerInventory.player);
    }

    @Override
    public void broadcastChanges()
    {
        // Called every tick and in other circumstances
        super.broadcastChanges();

        if (playerInventory.player instanceof ServerPlayer serverPlayer &&
                (initialSync || serverPlayer.level().getGameTime() % 20 * 8 == 0))
        {
            syncGraph.emitter().apply(be.getDataLog().asView(), be.getProblems());
            initialSync = false;
        }
    }

    public LivingMachineBlockEntity getBlockEntity()
    {
        return be;
    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);
        syncGraph.close();
    }

    public interface SyncGraph
    {
        void apply(DataLog.DataView dataView, List<Component> problems);
    }
}
