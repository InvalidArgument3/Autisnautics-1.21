/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.network;

import com.neep.meatlib.api.network.ChannelFormat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Screen channel. The dynamic-proxy {@link ChannelFormat} is unchanged. Transport is a
 * NeoForge {@link ChannelMessage} instead of a Fabric play channel.
 */
public final class ChannelManager<T> implements AutoCloseable {
    private final ResourceLocation name;
    private final ChannelFormat<T> format;
    private final Player player;
    private T emitter;

    public ChannelManager(ResourceLocation name, ChannelFormat<T> format, Player player) {
        this.name = name;
        this.format = format;
        this.player = player;
    }

    public static <T> ChannelManager<T> create(ResourceLocation name, ChannelFormat<T> format, Player player) {
        return new ChannelManager<>(name, format, player);
    }

    public T emitter() {
        if (emitter == null) {
            emitter = format.emitter(buf -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    MeatlibPayloads.sendToPlayer(serverPlayer, name, buf);
                } else {
                    MeatlibPayloads.sendToServer(name, buf);
                }
            });
        }
        return emitter;
    }

    public void receiver(T listener) {
        MeatlibPayloads.listen(name, (buf, received) -> {
            if (received == player) {
                format.receive(listener, buf, Runnable::run);
            }
        });
    }

    public ChannelFormat<T> format() {
        return format;
    }

    @Override
    public void close() {
        emitter = null;
    }

    public FriendlyByteBuf capture(java.util.function.Consumer<T> call) {
        io.netty.buffer.ByteBuf raw = io.netty.buffer.Unpooled.buffer();
        FriendlyByteBuf buf = new FriendlyByteBuf(raw);
        T local = format.emitter(outgoing -> buf.writeBytes(outgoing));
        call.accept(local);
        return buf;
    }
}
