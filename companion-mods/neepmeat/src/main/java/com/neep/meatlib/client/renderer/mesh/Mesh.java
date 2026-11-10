/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.client.renderer.mesh;

import net.minecraft.client.renderer.block.model.BakedQuad;

import java.util.List;

public final class Mesh {
    private final List<BakedQuad> quads;

    public Mesh(List<BakedQuad> quads) {
        this.quads = List.copyOf(quads);
    }

    public List<BakedQuad> quads() {
        return quads;
    }

    public void outputTo(QuadEmitter emitter) {
    }
}
