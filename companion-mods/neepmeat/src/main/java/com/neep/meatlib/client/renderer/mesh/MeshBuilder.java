/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.client.renderer.mesh;

public final class MeshBuilder {
    private final QuadEmitter emitter = new QuadEmitter();

    public QuadEmitter getEmitter() {
        return emitter;
    }

    public Mesh build() {
        Mesh mesh = new Mesh(emitter.baked(), emitter.snapshots());
        emitter.clearBaked();
        return mesh;
    }
}
