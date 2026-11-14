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

package com.neep.neepmeat.network;

import com.neep.neepmeat.NeepMeat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class ScreenPropertyC2SPacket
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "screen_int_update");

    public static void init()
    {
        ServerPlayNetworking.registerGlobalReceiver(ID, ScreenPropertyC2SPacket::apply);
    }

    private static void apply(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl networkHandler, FriendlyByteBuf buf, ServerPlayNetworking.PacketSender sender)
    {
        int id = buf.readVarInt();
        int value = buf.readVarInt();

        server.execute(() ->
        {
            var handler = player.containerMenu;
            if (handler != null)
            {
                handler.setData(id, value);
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public static class Client
    {
        public static void send(int id, int value)
        {
            ClientPlayNetworking.send(ID, create(id, value));
        }

        public static FriendlyByteBuf create(int id, int value)
        {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeVarInt(id);
            buf.writeVarInt(value);

            return buf;
        }
    }
}
