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
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

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
}
