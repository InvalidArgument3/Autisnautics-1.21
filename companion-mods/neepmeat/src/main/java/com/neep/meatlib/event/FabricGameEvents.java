/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric call sites keep their shape. The body forwards to the NeoForge event.
 */


package com.neep.meatlib.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Forwards Fabric {@code .register(lambda)} events onto the NeoForge game bus. */
@EventBusSubscriber(modid = "meatlib")
public final class FabricGameEvents {
    private FabricGameEvents() {}

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Pre event) {
        if (event.getLevel() instanceof ServerLevel level) {
            for (var listener : ServerTickEvents.START_WORLD_TICK.listeners()) {
                listener.onStartTick(level);
            }
        }
    }

    @SubscribeEvent
    public static void onStarting(ServerStartingEvent event) {
        for (var listener : ServerLifecycleEvents.SERVER_STARTING.listeners()) listener.onServer(event.getServer());
    }

    @SubscribeEvent
    public static void onStarted(ServerStartedEvent event) {
        for (var listener : ServerLifecycleEvents.SERVER_STARTED.listeners()) listener.onServer(event.getServer());
    }

    @SubscribeEvent
    public static void onStopping(ServerStoppingEvent event) {
        for (var listener : ServerLifecycleEvents.SERVER_STOPPING.listeners()) listener.onServer(event.getServer());
    }

    @SubscribeEvent
    public static void onStopped(ServerStoppedEvent event) {
        for (var listener : ServerLifecycleEvents.SERVER_STOPPED.listeners()) listener.onServer(event.getServer());
    }

    @SubscribeEvent
    public static void onLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            for (var listener : ServerWorldEvents.LOAD.listeners()) listener.onWorldLoad(level.getServer(), level);
        }
    }

    @SubscribeEvent
    public static void onUseBlock(PlayerInteractEvent.RightClickBlock event) {
        for (var listener : UseBlockCallback.EVENT.listeners()) {
            var result = listener.interact(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
            if (result != net.minecraft.world.InteractionResult.PASS) {
                event.setCancellationResult(result);
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        for (var listener : CommandRegistrationCallback.EVENT.listeners()) {
            listener.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
        }
    }

    @SubscribeEvent
    public static void onLoot(LootTableLoadEvent event) {
        for (var listener : LootTableEvents.MODIFY.listeners()) {
            listener.modify(null, null, event.getName(), null, null);
        }
    }
}
