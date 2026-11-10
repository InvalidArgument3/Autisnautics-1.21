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
     * Fabric's renderer is not running on NeoForge. Vanilla models still draw through {@code getQuads}.
     * Nothing in this port consumes the mesh passed here.
     */
    default Consumer<Mesh> meshConsumer() {
        return mesh -> {};
    }
}
