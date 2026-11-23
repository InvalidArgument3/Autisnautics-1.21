/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric reload listeners are applied on AddReloadListenerEvent and
 * RegisterClientReloadListenersEvent.
 */

package com.neep.meatlib.resource;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class FabricResources {
    private static final List<PreparableReloadListener> SERVER = new ArrayList<>();
    private static final List<PreparableReloadListener> CLIENT = new ArrayList<>();

    private FabricResources() {}

    public interface SimpleSynchronousResourceReloadListener extends PreparableReloadListener {
        ResourceLocation getFabricId();

        void reload(ResourceManager manager);

        @Override
        default CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler, Executor background, Executor game) {
            return barrier.wait(null).thenRunAsync(() -> reload(manager), game);
        }
    }

    public static final class ResourceManagerHelper {
        private final PackType type;

        private ResourceManagerHelper(PackType type) {
            this.type = type;
        }

        public static ResourceManagerHelper get(PackType type) {
            return new ResourceManagerHelper(type);
        }

        public void registerReloadListener(PreparableReloadListener listener) {
            if (type == PackType.CLIENT_RESOURCES) {
                CLIENT.add(listener);
            } else {
                SERVER.add(listener);
            }
        }
    }

    public static final class ResourceReloadListenerKeys {
        public static final ResourceLocation RECIPES = ResourceLocation.withDefaultNamespace("recipes");

        private ResourceReloadListenerKeys() {}
    }

    @EventBusSubscriber(modid = "meatlib")
    public static final class ServerHooks {
        private ServerHooks() {}

        @SubscribeEvent
        public static void onReload(AddReloadListenerEvent event) {
            for (PreparableReloadListener listener : SERVER) {
                event.addListener(listener);
            }
        }
    }

    @EventBusSubscriber(modid = "meatlib", value = net.neoforged.api.distmarker.Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class ClientHooks {
        private ClientHooks() {}

        @SubscribeEvent
        public static void onReload(RegisterClientReloadListenersEvent event) {
            for (PreparableReloadListener listener : CLIENT) {
                event.registerReloadListener(listener);
            }
        }
    }
}
