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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ServerChannelSender<T> implements Sender<T>
{
    private final ResourceLocation name;
    private final ChannelFormat<T> format;
    private final Player player;

    public ServerChannelSender(ResourceLocation name, ChannelFormat<T> format, Player player)
    {
        this.name = name;
        this.format = format;
        this.player = player;
    }

    @Override
    public void send(FriendlyByteBuf buf)
    {
        if (player instanceof ServerPlayer serverPlayerEntity)
        {
            ServerPlayNetworking.send(serverPlayerEntity, name, buf);
        }
    }
}
