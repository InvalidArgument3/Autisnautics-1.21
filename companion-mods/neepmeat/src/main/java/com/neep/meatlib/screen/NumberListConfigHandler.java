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

package com.neep.meatlib.screen;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.network.ChannelManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public abstract class NumberListConfigHandler<T, L extends List<T>> implements WidgetHandler
{
    public final ChannelManager<UpdateList<T, L>> updateParamsS2C;
    public final ChannelManager<UpdateList<T, L>> receiveParamsC2S;

    protected boolean updateToClient = true;

    protected NumberListConfigHandler(Player player, ParamCodec<L> listCodec)
    {
        updateParamsS2C = ChannelManager.create(
                ResourceLocation.fromNamespaceAndPath(MeatLib.ID, "update_values"),
                ChannelFormat.builder(UpdateList.<T, L>as()).param(listCodec).build(),
                player);

        receiveParamsC2S = ChannelManager.create(
                ResourceLocation.fromNamespaceAndPath(MeatLib.ID, "receive_values"),
                ChannelFormat.builder(UpdateList.<T, L>as()).param(listCodec).build(),
                player);

        receiveParamsC2S.receiver(this::receiveValuesC2S);
    }

    @Override
    public void sendUpdates()
    {
        if (updateToClient)
        {
            updateParamsS2C.emitter().update(getValues());
            updateToClient = false;
        }
    }

    @Override
    public void close()
    {
        updateParamsS2C.close();
        receiveParamsC2S.close();
    }

    protected void receiveValuesC2S(L list)
    {
        updateToClient = true;
        setValues(list);
    }

    protected abstract void setValues(L list);

    protected abstract L getValues();

    @FunctionalInterface
    public interface UpdateList<T, L extends Iterable<T>>
    {
        void update(L list);

        static <T, L extends Iterable<T>> Class<UpdateList<T, L>> as()
        {
            return (Class<UpdateList<T,L>>) (Object) UpdateList.class;
        }
    }
}
