/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric call sites keep their shape. The body forwards to the NeoForge event.
 */


package net.fabricmc.fabric.api.client.rendering.v1;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** Fabric's dynamic item renderer. NeoForge reads the same render through {@link IClientItemExtensions}. */
public final class BuiltinItemRendererRegistry {
    public static final BuiltinItemRendererRegistry INSTANCE = new BuiltinItemRendererRegistry();

    private BuiltinItemRendererRegistry() {}

    public void register(Item item, DynamicItemRenderer renderer) {
        DynamicItemRenderer.REGISTERED.put(item, renderer);
    }

    public interface DynamicItemRenderer {
        java.util.Map<Item, DynamicItemRenderer> REGISTERED = new java.util.HashMap<>();

        void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay);
    }

    @EventBusSubscriber(modid = "meatlib", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class BewlrHooks {
        private BewlrHooks() {}

        @SubscribeEvent
        public static void onExtensions(RegisterClientExtensionsEvent event) {
            for (Item item : BuiltInRegistries.ITEM) {
                var id = BuiltInRegistries.ITEM.getKey(item);
                if (id == null || !(id.getNamespace().equals("neepmeat") || id.getNamespace().equals("meatweapons") || id.getNamespace().equals("meatlib"))) {
                    continue;
                }
                event.registerItem(new LazyExtensions(item), item);
            }
        }

        /** Looks up the Fabric renderer when the item is drawn, not when the event fires. */
        private static final class LazyExtensions implements IClientItemExtensions {
            private final Item item;
            private BlockEntityWithoutLevelRenderer bewlr;

            private LazyExtensions(Item item) {
                this.item = item;
            }

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                DynamicItemRenderer renderer = DynamicItemRenderer.REGISTERED.get(item);
                if (renderer == null) {
                    return IClientItemExtensions.super.getCustomRenderer();
                }
                if (bewlr == null) {
                    Minecraft minecraft = Minecraft.getInstance();
                    bewlr = new BlockEntityWithoutLevelRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels()) {
                        @Override
                        public void renderByItem(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource buffers, int light, int overlay) {
                            DynamicItemRenderer current = DynamicItemRenderer.REGISTERED.get(item);
                            if (current != null) {
                                current.render(stack, mode, matrices, buffers, light, overlay);
                            }
                        }
                    };
                }
                return bewlr;
            }
        }
    }
}
