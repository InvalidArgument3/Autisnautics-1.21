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

package com.neep.meatlib.network.screen;

import com.neep.meatlib.api.network.ParamCodec;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import java.util.List;
import java.util.function.Consumer;

public class ScreenChannel<T>
{
    private final ResourceLocation name;
    private final ParamCodec<T> codec;
    private List<ServerHandler> serverHandlers;

    public static Sender server(ServerPlayer player)
    {
        return (channel, buf) -> ServerPlayNetworking.send(player, channel, buf);
    }

    public ScreenChannel(ResourceLocation name, ParamCodec<T> codec)
    {
        this.name = name;
        this.codec = codec;
    }

    public void send(T t, Sender sender)
    {
        FriendlyByteBuf buf = PacketByteBufs.create();
        codec.encode(t, buf);
        sender.send(name, buf);
    }

    public void attach(Consumer<T> consumer, ServerPlayer player)
    {
        serverHandlers.add(new ServerHandler(player, consumer));
    }

    public void close()
    {
        for (var handler : serverHandlers)
        {
            handler.close();
        }
    }

    private class ServerHandler
    {
        private final ServerPlayer player;
        private final Consumer<T> consumer;

        public ServerHandler(ServerPlayer player, Consumer<T> consumer)
        {
            this.player = player;
            this.consumer = consumer;
            ServerPlayNetworking.registerReceiver(player.connection, name, (server, player1, handler, buf, responseSender) ->
            {
                T t = codec.decode(buf);
                server.execute(() -> this.consumer.accept(t));
            });
        }

        public void close()
        {
            ServerPlayNetworking.unregisterReceiver(player.connection, name);
        }
    }

    private interface Sender
    {
        void send(ResourceLocation channel, FriendlyByteBuf buf);
    }
}
