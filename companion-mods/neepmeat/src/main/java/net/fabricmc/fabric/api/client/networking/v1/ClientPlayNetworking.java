/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Same shape as Fabric's ClientPlayNetworking.
 */

package net.fabricmc.fabric.api.client.networking.v1;

import com.neep.neepmeat.network.RawPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientPlayNetworking {
    private static final Map<ResourceLocation, PlayChannelHandler> HANDLERS = new ConcurrentHashMap<>();

    private ClientPlayNetworking() {}

    public static void send(ResourceLocation id, FriendlyByteBuf buf) {
        PacketDistributor.sendToServer(RawPayload.of(id, buf));
    }

    public static void registerGlobalReceiver(ResourceLocation id, PlayChannelHandler handler) {
        HANDLERS.put(id, handler);
    }

    public static void dispatch(Minecraft client, ResourceLocation id, FriendlyByteBuf buf) {
        PlayChannelHandler handler = HANDLERS.get(id);
        if (handler != null && client.getConnection() != null) {
            handler.receive(client, client.getConnection(), buf, new ResponseSender());
        }
    }

    @FunctionalInterface
    public interface PlayChannelHandler {
        void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, ResponseSender responseSender);
    }

    public static final class ResponseSender {
        public void sendPacket(ResourceLocation id, FriendlyByteBuf buf) {
            ClientPlayNetworking.send(id, buf);
        }
    }
}
