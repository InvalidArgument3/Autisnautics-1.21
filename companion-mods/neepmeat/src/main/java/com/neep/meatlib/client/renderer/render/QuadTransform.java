/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.client.renderer.render;

import com.neep.meatlib.client.renderer.mesh.MutableQuadView;

/**
 * Fabric's quad transform. Return false to drop the quad. Edits to the view are baked.
 */
@FunctionalInterface
public interface QuadTransform {
    boolean transform(MutableQuadView quad);
}
