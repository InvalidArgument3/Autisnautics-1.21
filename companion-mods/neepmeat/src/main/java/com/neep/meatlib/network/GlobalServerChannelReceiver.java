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

package com.neep.meatlib.network;

import com.neep.meatlib.api.network.ChannelFormat;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class GlobalServerChannelReceiver
{
    public static <T> void register(ResourceLocation channelName, ChannelFormat<T> format, T listener)
    {
        ServerPlayNetworking.registerGlobalReceiver(channelName, (server, player, handler, buf, responseSender) ->
                format.receive(listener, buf, server));
    }

    public static <T> void register(ResourceLocation channelName, ChannelFormat<T> format, GlobalChannelManager.ReceiveHandler<T> receiveHandler)
    {
        ServerPlayNetworking.registerGlobalReceiver(channelName, (server, player, handler, buf, responseSender) ->
                format.receive(receiveHandler.receive(player, buf, responseSender), buf, server));
    }
}
