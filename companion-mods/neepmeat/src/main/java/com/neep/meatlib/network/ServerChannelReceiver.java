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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import java.util.concurrent.Executor;

public class ServerChannelReceiver<T> implements Receiver<T>
{
    private final ResourceLocation name;
    private final ServerGamePacketListenerImpl handler;
    private final ChannelFormat<T> format;
    private final T listener;

    public ServerChannelReceiver(ServerGamePacketListenerImpl handler, ResourceLocation name, ChannelFormat<T> format, T listener)
    {
        this.name = name;
        this.handler = handler;
        this.format = format;
        this.listener = listener;
        ServerPlayNetworking.unregisterReceiver(handler, name); // Ensure that the latest receiver will replace the current
        ServerPlayNetworking.registerReceiver(handler, name, (server, player1, handler1, buf, responseSender) ->
                receive(buf, server));
    }

    private void receive(FriendlyByteBuf buf, Executor executor)
    {
        format.receive(listener, buf, executor);
    }

    @Override
    public void close()
    {
        ServerPlayNetworking.unregisterReceiver(handler, name);
    }
}
