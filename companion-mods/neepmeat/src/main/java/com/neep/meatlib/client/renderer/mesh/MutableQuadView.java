/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.client.renderer.mesh;

import net.minecraft.core.Direction;

public interface MutableQuadView {
    int BAKE_ROTATE_NONE = 0;
    int BAKE_ROTATE_90 = 1;
    int BAKE_ROTATE_180 = 2;
    int BAKE_ROTATE_270 = 3;
    int BAKE_LOCK_UV = 4;
    int BAKE_FLIP_U = 8;
    int BAKE_FLIP_V = 16;
    int BAKE_NORMALIZED = 32;

    default Direction nominalFace() {
        return Direction.NORTH;
    }
}
