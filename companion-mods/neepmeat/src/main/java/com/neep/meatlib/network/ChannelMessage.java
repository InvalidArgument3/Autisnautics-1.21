/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.network;

import com.neep.meatlib.MeatLib;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChannelMessage(ResourceLocation channel, byte[] payload) implements CustomPacketPayload {
    public static final Type<ChannelMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MeatLib.ID, "channel"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChannelMessage> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ChannelMessage::channel,
            ByteBufCodecs.BYTE_ARRAY, ChannelMessage::payload,
            ChannelMessage::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
