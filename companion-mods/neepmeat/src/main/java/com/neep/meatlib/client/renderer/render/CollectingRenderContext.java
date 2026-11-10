/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * meshConsumer keeps the mesh's baked quads. Emissive flags are not read.
 */

package com.neep.meatlib.client.renderer.render;

import com.neep.meatlib.client.renderer.mesh.Mesh;
import com.neep.meatlib.client.renderer.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class CollectingRenderContext implements RenderContext {
    private final List<BakedQuad> quads = new ArrayList<>();
    private final QuadEmitter emitter = new QuadEmitter();

    @Override
    public QuadEmitter getEmitter() {
        return emitter;
    }

    @Override
    public Consumer<Mesh> meshConsumer() {
        return mesh -> {
            if (mesh != null) {
                quads.addAll(mesh.quads());
            }
        };
    }

    public List<BakedQuad> quads(Direction side) {
        if (side == null) {
            return List.copyOf(quads);
        }
        List<BakedQuad> facing = new ArrayList<>();
        for (BakedQuad quad : quads) {
            if (quad.getDirection() == side) {
                facing.add(quad);
            }
        }
        return facing;
    }
}
