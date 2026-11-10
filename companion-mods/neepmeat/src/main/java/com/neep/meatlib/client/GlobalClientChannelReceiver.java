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

package com.neep.meatlib.client;

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.network.GlobalChannelManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

@OnlyIn(Dist.CLIENT)
public class GlobalClientChannelReceiver
{
    public static <T> void register(ResourceLocation name, ChannelFormat<T> format, T listener)
    {
        ClientPlayNetworking.registerGlobalReceiver(name, (client, handler, buf, responseSender) ->
                format.receive(listener, buf, client));
    }

    public static <T> void register(ResourceLocation name, ChannelFormat<T> format, GlobalChannelManager.ReceiveHandler<T> receiveHandler)
    {
        ClientPlayNetworking.registerGlobalReceiver(name, (client, handler, buf, responseSender) ->
                format.receive(receiveHandler.receive(client.player, buf, responseSender), buf, client));
    }
}