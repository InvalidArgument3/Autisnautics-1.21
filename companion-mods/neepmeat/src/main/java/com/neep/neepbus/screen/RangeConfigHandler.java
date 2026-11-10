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

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.network.ChannelManager;
import com.neep.meatlib.screen.WidgetHandler;
import com.neep.neepbus.part.Ranged;
import com.neep.neepmeat.NeepMeat;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class RangeConfigHandler implements WidgetHandler
{
    public final ChannelManager<SliderScreenHandler.UpdateParams> updateParamsS2C;
    public final ChannelManager<SliderScreenHandler.UpdateParams> receiveParamsC2S;
    protected final Ranged ranged;
    protected boolean updateToClient = true;

    public RangeConfigHandler(Player player, Ranged ranged)
    {
        updateParamsS2C = ChannelManager.create(
                ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "update_params"),
                ChannelFormat.builder(SliderScreenHandler.UpdateParams.class).param(ParamCodec.list(ParamCodec.FLOAT)).build(),
                player);

        receiveParamsC2S = ChannelManager.create(
                ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "receive_params"),
                ChannelFormat.builder(SliderScreenHandler.UpdateParams.class).param(ParamCodec.list(ParamCodec.FLOAT)).build(),
                player);
        this.ranged = ranged;

        receiveParamsC2S.receiver(this::receiveParams);
    }

    protected void receiveParams(List<Float> ints)
    {
        updateToClient = true;

        ranged.setValues(ints.get(0), ints.get(1), ints.get(2));
    }

    public void markNeedsUpdate()
    {
        updateToClient = true;
    }

    @Override
    public void sendUpdates()
    {
        if (updateToClient)
        {
            updateParamsS2C.emitter().update(List.of(ranged.getValue(), ranged.getMinValue(), ranged.getMaxValue()));
            updateToClient = false;
        }
    }

    @Override
    public void close()
    {
        updateParamsS2C.close();
        receiveParamsC2S.close();
    }
}
