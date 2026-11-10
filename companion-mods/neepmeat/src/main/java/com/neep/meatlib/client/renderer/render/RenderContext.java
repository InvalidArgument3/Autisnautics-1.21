/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.client.renderer.render;

import com.neep.meatlib.client.renderer.mesh.Mesh;
import com.neep.meatlib.client.renderer.mesh.QuadEmitter;

import java.util.function.Consumer;

public interface RenderContext {
    QuadEmitter getEmitter();

    default void pushTransform(Object transform) {}

    default void popTransform() {}

    /**
     * Receives a mesh whose quads were baked from stored vertex positions, UVs, colours, and normals.
     * {@link CollectingRenderContext} keeps those quads for {@code getQuads}. Emissive flags are ignored.
     */
    default Consumer<Mesh> meshConsumer() {
        return mesh -> {};
    }
}
