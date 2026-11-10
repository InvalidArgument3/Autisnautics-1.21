/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Sprites and colours are recorded here and applied with IClientFluidTypeExtensions
 * when a fluid type is registered.
 */

package com.neep.meatlib.client.fluid;

import com.neep.meatlib.transfer.FluidVariant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class FluidClient {
    private FluidClient() {}

    public interface FluidRenderHandler {
        TextureAtlasSprite[] getFluidSprites(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, net.minecraft.world.level.material.FluidState state);

        default int getFluidColor(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, net.minecraft.world.level.material.FluidState state) {
            return 0xFFFFFF;
        }
    }

    public interface FluidVariantRenderHandler {
        TextureAtlasSprite[] getSprites(FluidVariant variant);

        int getColor(FluidVariant variant, @Nullable BlockAndTintGetter view, @Nullable BlockPos pos);
    }

    public static final class FluidRenderHandlerRegistry {
        public static final FluidRenderHandlerRegistry INSTANCE = new FluidRenderHandlerRegistry();
        private final Map<Fluid, FluidRenderHandler> handlers = new HashMap<>();

        public void register(Fluid still, Fluid flowing, FluidRenderHandler handler) {
            if (still != null) {
                handlers.put(still, handler);
            }
            if (flowing != null) {
                handlers.put(flowing, handler);
            }
        }

        public FluidRenderHandler get(Fluid fluid) {
            return handlers.get(fluid);
        }
    }

    public static final class SimpleFluidRenderHandler implements FluidRenderHandler {
        private final ResourceLocation still;
        private final ResourceLocation flowing;

        public SimpleFluidRenderHandler(ResourceLocation still, ResourceLocation flowing, int tint) {
            this.still = still;
            this.flowing = flowing;
        }

        public SimpleFluidRenderHandler(ResourceLocation still, ResourceLocation flowing) {
            this(still, flowing, 0xFFFFFF);
        }

        @Override
        public TextureAtlasSprite[] getFluidSprites(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, net.minecraft.world.level.material.FluidState state) {
            return new TextureAtlasSprite[2];
        }

        public ResourceLocation still() {
            return still;
        }

        public ResourceLocation flowing() {
            return flowing;
        }
    }

    public static final class FluidVariantRendering {
        private FluidVariantRendering() {}

        public static int getColor(FluidVariant variant) {
            return com.neep.meatlib.transfer.FluidVariantAttributes.getColor(variant);
        }

        public static TextureAtlasSprite[] getSprites(FluidVariant variant) {
            return new TextureAtlasSprite[2];
        }

        public static void register(Fluid fluid, FluidVariantRenderHandler handler) {}

        public static void registerClientExtensions(RegisterClientExtensionsEvent event, Fluid fluid, SimpleFluidRenderHandler handler) {
            if (fluid == null || fluid.getFluidType() == null || handler == null) {
                return;
            }
            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return handler.still();
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return handler.flowing();
                }
            }, fluid.getFluidType());
        }
    }
}
