/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.client.renderer.render;

import com.neep.meatlib.client.renderer.mesh.QuadEmitter;

public interface RenderContext {
    QuadEmitter getEmitter();

    default void pushTransform(Object transform) {}

    default void popTransform() {}
}
