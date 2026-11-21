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
import com.neep.meatlib.event.RegisterEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

public final class WorldRenderEvents {
    public static final RegisterEvent<AfterEntities> AFTER_ENTITIES = new RegisterEvent<>();
    public static final RegisterEvent<AfterTranslucent> AFTER_TRANSLUCENT = new RegisterEvent<>();
    public static final RegisterEvent<BlockOutline> BLOCK_OUTLINE = new RegisterEvent<>();
    public static final RegisterEvent<Last> LAST = new RegisterEvent<>();

    private WorldRenderEvents() {}

    @FunctionalInterface public interface AfterEntities { void afterEntities(WorldRenderContext context); }
    @FunctionalInterface public interface AfterTranslucent { void afterTranslucent(WorldRenderContext context); }
    @FunctionalInterface public interface Last { void last(WorldRenderContext context); }
    @FunctionalInterface public interface BlockOutline { boolean beforeBlockOutline(WorldRenderContext context, BlockOutlineContext outline); }

    public static final class WorldRenderContext {
        private final PoseStack matrices;
        private final Matrix4f projection;
        private final MultiBufferSource consumers;
        private final Camera camera;
        private final float tickDelta;

        public WorldRenderContext(PoseStack matrices, Matrix4f projection, MultiBufferSource consumers, Camera camera, float tickDelta) {
            this.matrices = matrices;
            this.projection = projection;
            this.consumers = consumers;
            this.camera = camera;
            this.tickDelta = tickDelta;
        }

        public PoseStack matrixStack() { return matrices; }
        public Matrix4f projectionMatrix() { return projection; }
        public MultiBufferSource consumers() { return consumers; }
        public Camera camera() { return camera; }
        public float tickDelta() { return tickDelta; }
    }

    public static final class BlockOutlineContext {
        public net.minecraft.core.BlockPos blockPos() { return net.minecraft.core.BlockPos.ZERO; }
    }
}
