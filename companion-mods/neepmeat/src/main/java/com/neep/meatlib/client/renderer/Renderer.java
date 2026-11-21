/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.client.renderer;

import com.neep.meatlib.client.renderer.mesh.MeshBuilder;

public final class Renderer {
    public static final Renderer INSTANCE = new Renderer();

    public MeshBuilder meshBuilder() {
        return new MeshBuilder();
    }
}
