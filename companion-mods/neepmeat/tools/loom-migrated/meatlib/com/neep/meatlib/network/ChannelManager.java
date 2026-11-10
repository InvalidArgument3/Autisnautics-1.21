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
import com.neep.meatlib.client.ClientChannelReceiver;
import com.neep.meatlib.client.ClientChannelSender;
import com.neep.meatlib.screen.Closable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ChannelManager<T> implements Closable
{
    private final ResourceLocation name;
    private final ChannelFormat<T> format;
    private final Player player;

    private final List<Receiver<T>> receivers = new ArrayList<>();
    private final Sender<T> sender;

    @Nullable private T emitter;

    public static <T> ChannelManager<T> create(ResourceLocation name, ChannelFormat<T> format, Player player)
    {
        // If sources are split in the future, this will be redirected via an opaque interface
        // that changes depending on environment.
        return new ChannelManager<>(name, format, player);
    }

    protected ChannelManager(ResourceLocation name, ChannelFormat<T> format, Player player)
    {
        this.name = name;
        this.format = format;
        this.player = player;
        this.sender = createSender(player);
    }

    private Sender<T> createSender(Player player)
    {
        if (player instanceof ServerPlayer serverPlayerEntity)
        {
            return new ServerChannelSender<>(name, format, serverPlayerEntity);
        }
        else
        {
            return new ClientChannelSender<>(name, format);
        }
    }

    public ChannelFormat<T> format()
    {
        return format;
    }

    public T emitter()
    {
        // I assume that creating a proxy instance is slow, so the emitter is cached.
        if (emitter == null)
            emitter =  format.emitter(sender);

        return emitter;
    }

    public void receiver(T listener)
    {
        Receiver<T> receiver;

        if (player instanceof ServerPlayer serverPlayer)
        {
            receiver = new ServerChannelReceiver<>(serverPlayer.connection, name, format, listener);
            receivers.add(receiver);
        }
        else
        {
            receiver = new ClientChannelReceiver<>(name, format, listener);
            receivers.add(receiver);
        }
    }

    @Override
    public void close()
    {
        receivers.forEach(Receiver::close);
    }
}
