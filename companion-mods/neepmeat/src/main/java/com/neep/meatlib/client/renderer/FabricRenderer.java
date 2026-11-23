/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 *
 * Lossy: emissive flags, colour indexes, and geometry that is not a unit square
 * are dropped. Quads are baked with QuadBakingVertexConsumer into BakedQuad lists.
 */

package com.neep.meatlib.client.renderer;

import com.neep.meatlib.client.renderer.mesh.Mesh;
import com.neep.meatlib.client.renderer.mesh.MeshBuilder;
import com.neep.meatlib.client.renderer.mesh.MutableQuadView;
import com.neep.meatlib.client.renderer.mesh.QuadEmitter;
import com.neep.meatlib.client.renderer.model.FabricBakedModel;
import com.neep.meatlib.client.renderer.render.RenderContext;

public final class FabricRenderer {
    private FabricRenderer() {}

    public static final class RendererAccess {
        public static final RendererAccess INSTANCE = new RendererAccess();

        public Renderer getRenderer() {
            return Renderer.INSTANCE;
        }
    }

    public static final class Renderer {
        public static final Renderer INSTANCE = new Renderer();

        public MeshBuilder meshBuilder() {
            return new MeshBuilder();
        }
    }
}
