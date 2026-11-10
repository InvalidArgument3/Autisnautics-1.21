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

package com.neep.neepmeat.api.network;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.enlightenment.EnlightenmentEvent;
import com.neep.neepmeat.enlightenment.EnlightenmentEventManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class EnlightenmentEventPacket
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "enlightenment_event");

    public static void send(EnlightenmentEvent.Factory factory, ServerLevel world, ServerPlayer player)
    {
        ServerPlayNetworking.send(player, ID, create(factory, world, player));
    }

    public static FriendlyByteBuf create(EnlightenmentEvent.Factory factory, ServerLevel world, ServerPlayer player)
    {
        if (world.isClientSide)
            throw new IllegalStateException("packet create called on the client!");

        FriendlyByteBuf byteBuf = PacketByteBufs.create();
        byteBuf.writeVarInt(EnlightenmentEventManager.EVENTS.getId(factory));
        byteBuf.writeResourceLocation(world.dimension().location());

        return byteBuf;
    }

    @Environment(value= Dist.CLIENT)
    public static class Client
    {
        public static void init()
        {
            ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, byteBuf, responseSender) ->
            {
                EnlightenmentEvent.Factory factory = EnlightenmentEventManager.EVENTS.byId(byteBuf.readVarInt());
                ResourceLocation worldId = byteBuf.readResourceLocation();

                client.execute(() ->
                {
                    ClientLevel world;
                    if ((world = Minecraft.getInstance().level) == null)
                        throw new IllegalStateException("Tried to spawn enlightenment effect in a null world!");

                    if (world.dimension().location().equals(worldId))
                    {
//                        EnlightenmentEventClient.spawnEvent(factory.create(world, client.player));
                    }
                });
            });
        }
    }
}
