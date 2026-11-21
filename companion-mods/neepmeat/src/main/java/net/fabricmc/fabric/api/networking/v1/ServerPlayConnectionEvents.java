/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Join, init, and disconnect ride on the player login and logout events.
 */

package net.fabricmc.fabric.api.networking.v1;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = "neepmeat")
public final class ServerPlayConnectionEvents {
    public static final Event<Init> INIT = new Event<>(Init.class, listeners -> (handler, server) -> {
        for (Init listener : listeners) {
            listener.onInit(handler, server);
        }
    });
    public static final Event<Init> DISCONNECT = new Event<>(Init.class, listeners -> (handler, server) -> {
        for (Init listener : listeners) {
            listener.onInit(handler, server);
        }
    });
    public static final Event<Join> JOIN = new Event<>(Join.class, listeners -> (handler, sender, server) -> {
        for (Join listener : listeners) {
            listener.onJoin(handler, sender, server);
        }
    });

    private ServerPlayConnectionEvents() {}

    public interface Init {
        void onInit(ServerGamePacketListenerImpl handler, MinecraftServer server);
    }

    public interface Join {
        void onJoin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            INIT.invoker().onInit(player.connection, player.server);
            JOIN.invoker().onJoin(player.connection, new PacketSender(), player.server);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DISCONNECT.invoker().onInit(player.connection, player.server);
        }
    }
}
