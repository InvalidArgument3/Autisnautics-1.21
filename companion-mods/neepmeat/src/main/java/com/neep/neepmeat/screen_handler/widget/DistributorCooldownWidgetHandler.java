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

package com.neep.neepmeat.screen_handler.widget;

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.network.ChannelManager;
import com.neep.meatlib.screen.WidgetHandler;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.machine.live_machine.block.entity.DistributorPointBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class DistributorCooldownWidgetHandler implements WidgetHandler
{
    public final ChannelManager<SyncCooldown> syncCooldownS2C;
    private final DistributorPointBlockEntity be;

    public DistributorCooldownWidgetHandler(Player playerEntity, DistributorPointBlockEntity be)
    {
        syncCooldownS2C = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "distributor_sync_cooldown"),
                ChannelFormat.builder(SyncCooldown.class).param(ParamCodec.INT).param(ParamCodec.INT).param(ParamCodec.BOOLEAN).build(), playerEntity);
        this.be = be;
    }

    @Override
    public void sendUpdates()
    {
        syncCooldownS2C.emitter().apply(be.getCooldown(), be.getMaxCooldown(), be.isPowered());
    }

    @Override
    public void close()
    {
        syncCooldownS2C.close();
    }

    public interface SyncCooldown
    {
        void apply(int cooldown, int maxCooldown, boolean powered);
    }
}
