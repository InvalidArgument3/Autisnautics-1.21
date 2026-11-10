/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Same shape as Fabric's ServerPlayNetworking. Bytes ride on one CustomPacketPayload.
 */

package net.fabricmc.fabric.api.networking.v1;

import com.neep.neepmeat.network.RawPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerPlayNetworking {
    private static final Map<ResourceLocation, PlayChannelHandler> HANDLERS = new ConcurrentHashMap<>();

    private ServerPlayNetworking() {}

    public static void send(ServerPlayer player, ResourceLocation id, FriendlyByteBuf buf) {
        PacketDistributor.sendToPlayer(player, RawPayload.of(id, buf));
    }

    public static void registerGlobalReceiver(ResourceLocation id, PlayChannelHandler handler) {
        HANDLERS.put(id, handler);
    }

    public static void dispatch(MinecraftServer server, ServerPlayer player, ResourceLocation id, FriendlyByteBuf buf) {
        PlayChannelHandler handler = HANDLERS.get(id);
        if (handler != null) {
            handler.receive(server, player, player.connection, buf, new PacketSender());
        }
    }

    @FunctionalInterface
    public interface PlayChannelHandler {
        void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender);
    }

    public static final class PacketSender {
        public void sendPacket(ResourceLocation id, FriendlyByteBuf buf) {}
    }
}
