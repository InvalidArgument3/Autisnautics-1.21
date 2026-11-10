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

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.client.ClientChannelSender;
import com.neep.meatlib.client.GlobalClientChannelReceiver;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class GlobalChannelManager<T>
{
    private final ResourceLocation name;
    private final ChannelFormat<T> format;

    // If sources are split in the future, I may need to instantiate this through an opaque functional interface
    // that changes depending on environment.
    public static <T> GlobalChannelManager<T> create(ResourceLocation name, ChannelFormat<T> format)
    {
        return new GlobalChannelManager<>(name, format);
    }

    protected GlobalChannelManager(ResourceLocation name, ChannelFormat<T> format)
    {
        this.name = name;
        this.format = format;
    }

    private Sender<T> createSender(@Nullable Player player)
    {
        if (player instanceof ServerPlayer serverPlayerEntity)
        {
            return buf -> ServerPlayNetworking.send(serverPlayerEntity, name, buf);
        }
        else
        {
            // This should be server-safe as it will never be called on the server, but it's a bit naughty.
            return new ClientChannelSender<>(name, format);
        }
    }

    public ChannelFormat<T> format()
    {
        return format;
    }

    public T emitter(@Nullable Player to)
    {
        // TODO: Look into caching senders in a map
        return format.emitter(createSender(to));
    }

    /// Does nothing if envType does not match the current environment.
    public void receiver(EnvType envType, T listener)
    {
        if (envType == EnvType.SERVER && !MeatLib.isClient())
        {
            GlobalServerChannelReceiver.register(name, format, listener);
        }
        else if (MeatLib.isClient())
        {
            GlobalClientChannelReceiver.register(name, format, listener);
        }
    }

    public void receiverHandler(EnvType envType, ReceiveHandler<T> handler)
    {
        if (envType == EnvType.SERVER)
        {
            GlobalServerChannelReceiver.register(name, format, handler);
        }
        else
        {
            GlobalClientChannelReceiver.register(name, format, handler);
        }
    }

    public interface ReceiveHandler<T>
    {
        T receive(Player player, FriendlyByteBuf buf, PacketSender responseSender);
    }
}
