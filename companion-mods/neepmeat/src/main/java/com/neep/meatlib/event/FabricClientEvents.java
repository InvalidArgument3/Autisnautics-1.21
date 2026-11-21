/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric call sites keep their shape. The body forwards to the NeoForge event.
 */


package com.neep.meatlib.event;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Queues Fabric client registration and applies it on the NeoForge mod bus. */
@EventBusSubscriber(modid = "meatlib", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FabricClientEvents {
    private static final List<EntityEntry> ENTITIES = new ArrayList<>();
    private static final List<BlockEntityEntry> BLOCK_ENTITIES = new ArrayList<>();
    private static final List<BlockColorEntry> BLOCK_COLORS = new ArrayList<>();
    private static final List<ItemColorEntry> ITEM_COLORS = new ArrayList<>();
    private static final List<ModelLoadingPlugin> MODEL_PLUGINS = new ArrayList<>();
    private static final List<ParticleEntry> PARTICLES = new ArrayList<>();
    private static final List<SpriteEntry> SPRITE_PARTICLES = new ArrayList<>();
    private static final List<ScreenEntry> SCREENS = new ArrayList<>();
    private static final List<KeyMapping> KEYS = new ArrayList<>();
    private static final List<LayerEntry> LAYERS = new ArrayList<>();
    private static boolean gameBus = false;

    private FabricClientEvents() {}

    public static <T extends Entity> void entityRenderer(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
        ENTITIES.add(new EntityEntry(type, provider));
    }

    public static <T extends BlockEntity> void blockEntityRenderer(BlockEntityType<T> type, BlockEntityRendererProvider<T> provider) {
        BLOCK_ENTITIES.add(new BlockEntityEntry(type, provider));
    }

    public static void blockColor(BlockColor color, Block... blocks) {
        BLOCK_COLORS.add(new BlockColorEntry(color, blocks));
    }

    public static void itemColor(ItemColor color, ItemLike... items) {
        ITEM_COLORS.add(new ItemColorEntry(color, items));
    }

    public static void modelPlugin(ModelLoadingPlugin plugin) {
        MODEL_PLUGINS.add(plugin);
    }

    public static <T extends ParticleOptions> void particle(ParticleType<T> type, ParticleProvider<T> provider) {
        PARTICLES.add(new ParticleEntry(type, provider));
    }

    public static <T extends ParticleOptions> void particleSprite(ParticleType<T> type, ParticleEngine.SpriteParticleRegistration<T> registration) {
        SPRITE_PARTICLES.add(new SpriteEntry(type, registration));
    }

    public static <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void screen(MenuType<? extends M> type, MenuScreens.ScreenConstructor<M, U> constructor) {
        SCREENS.add(new ScreenEntry(type, constructor));
    }

    public static void key(KeyMapping key) {
        KEYS.add(key);
    }

    public static void layer(net.minecraft.client.model.geom.ModelLayerLocation location, java.util.function.Supplier<net.minecraft.client.model.geom.builders.LayerDefinition> supplier) {
        LAYERS.add(new LayerEntry(location, supplier));
    }

    private static void gameBus() {
        if (!gameBus) {
            gameBus = true;
            NeoForge.EVENT_BUS.addListener(FabricClientEvents::onRenderStage);
        }
    }

    @SubscribeEvent
    public static void onLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (LayerEntry entry : LAYERS) {
            event.registerLayerDefinition(entry.location, entry.supplier);
        }
    }

    @SubscribeEvent
    public static void onRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (EntityEntry entry : ENTITIES) {
            registerEntity(event, entry);
        }
        for (BlockEntityEntry entry : BLOCK_ENTITIES) {
            registerBlockEntity(event, entry);
        }
        gameBus();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerEntity(EntityRenderersEvent.RegisterRenderers event, EntityEntry entry) {
        event.registerEntityRenderer((EntityType) entry.type, (EntityRendererProvider) entry.provider);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerBlockEntity(EntityRenderersEvent.RegisterRenderers event, BlockEntityEntry entry) {
        event.registerBlockEntityRenderer((BlockEntityType) entry.type, (BlockEntityRendererProvider) entry.provider);
    }

    @SubscribeEvent
    public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        for (BlockColorEntry entry : BLOCK_COLORS) {
            event.register(entry.color, entry.blocks);
        }
    }

    @SubscribeEvent
    public static void onItemColors(RegisterColorHandlersEvent.Item event) {
        for (ItemColorEntry entry : ITEM_COLORS) {
            event.register(entry.color, entry.items);
        }
    }

    @SubscribeEvent
    public static void onModels(ModelEvent.RegisterAdditional event) {
        ModelLoadingPlugin.Context context = ids -> {
            for (ResourceLocation id : ids) {
                event.register(ModelResourceLocation.standalone(id));
            }
        };
        for (ModelLoadingPlugin plugin : MODEL_PLUGINS) {
            plugin.onInitializeModelLoader(context);
        }
    }

    @SubscribeEvent
    public static void onParticles(RegisterParticleProvidersEvent event) {
        for (ParticleEntry entry : PARTICLES) {
            registerParticle(event, entry);
        }
        for (SpriteEntry entry : SPRITE_PARTICLES) {
            registerSprite(event, entry);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerParticle(RegisterParticleProvidersEvent event, ParticleEntry entry) {
        event.registerSpecial((ParticleType) entry.type, (ParticleProvider) entry.provider);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerSprite(RegisterParticleProvidersEvent event, SpriteEntry entry) {
        event.registerSpriteSet((ParticleType) entry.type, (ParticleEngine.SpriteParticleRegistration) entry.registration);
    }

    @SubscribeEvent
    public static void onScreens(RegisterMenuScreensEvent event) {
        for (ScreenEntry entry : SCREENS) {
            registerScreen(event, entry);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerScreen(RegisterMenuScreensEvent event, ScreenEntry entry) {
        event.register((MenuType) entry.type, (MenuScreens.ScreenConstructor) entry.constructor);
    }

    @SubscribeEvent
    public static void onKeys(RegisterKeyMappingsEvent event) {
        for (KeyMapping key : KEYS) {
            event.register(key);
        }
    }

    @SubscribeEvent
    public static void onHud(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath("meatlib", "fabric_hud"), (graphics, delta) -> {
            float tick = delta.getGameTimeDeltaPartialTick(false);
            for (HudRenderCallback callback : HudRenderCallback.EVENT.listeners()) {
                callback.onHudRender(graphics, tick);
            }
        });
        gameBus();
    }

    private static void onRenderStage(RenderLevelStageEvent event) {
        var context = new WorldRenderEvents.WorldRenderContext(
                event.getPoseStack(),
                event.getProjectionMatrix(),
                Minecraft.getInstance().renderBuffers().bufferSource(),
                event.getCamera(),
                event.getPartialTick().getGameTimeDeltaPartialTick(false));
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            for (var listener : WorldRenderEvents.AFTER_ENTITIES.listeners()) {
                listener.afterEntities(context);
            }
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            for (var listener : WorldRenderEvents.AFTER_TRANSLUCENT.listeners()) {
                listener.afterTranslucent(context);
            }
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            for (var listener : WorldRenderEvents.LAST.listeners()) {
                listener.last(context);
            }
        }
    }

    private record EntityEntry(EntityType<?> type, EntityRendererProvider<?> provider) {}
    private record BlockEntityEntry(BlockEntityType<?> type, BlockEntityRendererProvider<?> provider) {}
    private record BlockColorEntry(BlockColor color, Block[] blocks) {}
    private record ItemColorEntry(ItemColor color, ItemLike[] items) {}
    private record ParticleEntry(ParticleType<?> type, ParticleProvider<?> provider) {}
    private record SpriteEntry(ParticleType<?> type, ParticleEngine.SpriteParticleRegistration<?> registration) {}
    private record ScreenEntry(MenuType<?> type, MenuScreens.ScreenConstructor<?, ?> constructor) {}
    private record LayerEntry(net.minecraft.client.model.geom.ModelLayerLocation location, java.util.function.Supplier<net.minecraft.client.model.geom.builders.LayerDefinition> supplier) {}
}
