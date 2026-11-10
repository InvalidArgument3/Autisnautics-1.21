/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

public final class MeatlibPayloads {
    private static final ConcurrentHashMap<ResourceLocation, List<BiConsumer<FriendlyByteBuf, net.minecraft.world.entity.player.Player>>> RECEIVERS = new ConcurrentHashMap<>();

    private MeatlibPayloads() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(com.neep.neepmeat.network.TankMessagePacket.TYPE, com.neep.neepmeat.network.TankMessagePacket.STREAM_CODEC, (packet, context) ->
                context.enqueueWork(() -> {
                    if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
                        com.neep.neepmeat.NeepMeatClient.showTank(packet);
                    }
                }));
        registrar.playBidirectional(ChannelMessage.TYPE, ChannelMessage.STREAM_CODEC, (message, context) -> {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(message.payload()));
            context.enqueueWork(() -> {
                List<BiConsumer<FriendlyByteBuf, net.minecraft.world.entity.player.Player>> listeners = RECEIVERS.get(message.channel());
                if (listeners == null) {
                    return;
                }
                for (BiConsumer<FriendlyByteBuf, net.minecraft.world.entity.player.Player> listener : listeners) {
                    buf.readerIndex(0);
                    listener.accept(buf, context.player());
                }
            });
        });
    }

    public static void listen(ResourceLocation channel, BiConsumer<FriendlyByteBuf, net.minecraft.world.entity.player.Player> listener) {
        RECEIVERS.computeIfAbsent(channel, ignored -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public static void sendToServer(ResourceLocation channel, FriendlyByteBuf buf) {
        PacketDistributor.sendToServer(new ChannelMessage(channel, bytes(buf)));
    }

    public static void sendToPlayer(net.minecraft.server.level.ServerPlayer player, ResourceLocation channel, FriendlyByteBuf buf) {
        PacketDistributor.sendToPlayer(player, new ChannelMessage(channel, bytes(buf)));
    }

    private static byte[] bytes(FriendlyByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        return data;
    }
}
