/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * One payload carries the old Fabric channel id and its bytes.
 */

package com.neep.neepmeat.network;

import com.neep.neepmeat.NeepMeat;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RawPayload(ResourceLocation channel, byte[] body) implements CustomPacketPayload {
    public static final Type<RawPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "raw"));
    public static final StreamCodec<FriendlyByteBuf, RawPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, RawPayload::channel,
            ByteBufCodecs.BYTE_ARRAY, RawPayload::body,
            RawPayload::new);

    public static RawPayload of(ResourceLocation channel, FriendlyByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        return new RawPayload(channel, bytes);
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(NeepMeat.ID).playBidirectional(TYPE, STREAM_CODEC, RawPayload::handle);
    }

    private void handle(IPayloadContext context) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(body));
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ServerPlayNetworking.dispatch(player.server, player, channel, buf);
            } else {
                ClientPlayNetworking.dispatch(Minecraft.getInstance(), channel, buf);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
