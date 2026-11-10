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
import com.neep.meatlib.network.Sender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class ClientChannelSender<T> implements Sender<T>
{
    private final ResourceLocation name;
    private final ChannelFormat<T> format;

    public ClientChannelSender(ResourceLocation name, ChannelFormat<T> format)
    {
        this.name = name;
        this.format = format;
    }

    @Override
    public void send(FriendlyByteBuf buf)
    {
        ClientPlayNetworking.send(name, buf);
    }
}
