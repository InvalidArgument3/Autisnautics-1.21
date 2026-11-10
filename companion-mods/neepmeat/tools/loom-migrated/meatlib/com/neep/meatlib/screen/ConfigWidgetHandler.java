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
import com.neep.meatlib.network.ChannelFormatFormatImpl;
import com.neep.meatlib.network.ChannelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public abstract class ConfigWidgetHandler<T> implements WidgetHandler
{
    protected boolean updateToClient = true;

    public final ChannelManager<Update<T>> updateParamsS2C;
    public final ChannelManager<Update<T>> receiveParamsC2S;

    private static <T> ChannelFormatFormatImpl.Builder<Update<T>> formatBuilder()
    {
        return ChannelFormat.builder(((Class<Update<T>>) (Object) Update.class));
    }

    protected ConfigWidgetHandler(Player player, ParamCodec<T> paramCodec)
    {
        this.updateParamsS2C = ChannelManager.create(
                new ResourceLocation(MeatLib.NAMESPACE, "update_values"),
                ConfigWidgetHandler.<T>formatBuilder().param(paramCodec).build(),
                player);

        this.receiveParamsC2S = ChannelManager.create(
                new ResourceLocation(MeatLib.NAMESPACE, "receive_values"),
                ConfigWidgetHandler.<T>formatBuilder().param(paramCodec).build(),
                player);

        receiveParamsC2S.receiver(this::receiveValuesC2S);
    }

    protected abstract T getValues();

    protected abstract void setValues(T t);

    private void receiveValuesC2S(T t)
    {
//        if (initialSynced)
        {
            updateToClient = true;
            setValues(t);
        }
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

    public interface Update<T>
    {
        void update(T t);
    }
}
