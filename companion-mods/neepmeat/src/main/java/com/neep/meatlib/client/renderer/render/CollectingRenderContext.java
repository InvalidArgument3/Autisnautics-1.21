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
import com.neep.meatlib.client.renderer.mesh.MutableQuadView;
import com.neep.meatlib.client.renderer.mesh.QuadEmitter;
import com.neep.meatlib.client.renderer.mesh.QuadSnapshot;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class CollectingRenderContext implements RenderContext {
    private final List<BakedQuad> quads = new ArrayList<>();
    private final QuadEmitter emitter = new QuadEmitter();
    private final ArrayDeque<QuadTransform> transforms = new ArrayDeque<>();

    @Override
    public QuadEmitter getEmitter() {
        return emitter;
    }

    @Override
    public void pushTransform(QuadTransform transform) {
        if (transform != null) {
            transforms.addLast(transform);
        }
    }

    @Override
    public void popTransform() {
        if (!transforms.isEmpty()) {
            transforms.removeLast();
        }
    }

    @Override
    public Consumer<Mesh> meshConsumer() {
        return mesh -> {
            if (mesh == null) {
                return;
            }
            if (!mesh.snapshots().isEmpty()) {
                for (QuadSnapshot snapshot : mesh.snapshots()) {
                    QuadSnapshot copy = snapshot.copy();
                    if (keep(copy)) {
                        quads.add(copy.bake());
                    }
                }
                return;
            }
            for (BakedQuad quad : mesh.quads()) {
                if (keep(new FaceView(quad.getDirection()))) {
                    quads.add(quad);
                }
            }
        };
    }

    public static void emitModel(RenderContext context, net.minecraft.client.resources.model.BakedModel model, net.minecraft.util.RandomSource random) {
        if (context == null || model == null) {
            return;
        }
        net.minecraft.util.RandomSource source = random == null ? net.minecraft.util.RandomSource.create() : random;
        List<BakedQuad> all = new ArrayList<>(model.getQuads(null, null, source));
        for (Direction direction : Direction.values()) {
            all.addAll(model.getQuads(null, direction, source));
        }
        if (context instanceof CollectingRenderContext collecting) {
            collecting.acceptBaked(all);
            return;
        }
        context.meshConsumer().accept(new com.neep.meatlib.client.renderer.mesh.Mesh(all));
    }

    /** Quads from a vanilla baked part, still passed through the transform stack. */
    public void acceptBaked(List<BakedQuad> baked) {
        if (baked == null) {
            return;
        }
        for (BakedQuad quad : baked) {
            if (keep(new FaceView(quad.getDirection()))) {
                quads.add(quad);
            }
        }
    }

    private boolean keep(MutableQuadView view) {
        for (QuadTransform transform : transforms) {
            if (!transform.transform(view)) {
                return false;
            }
        }
        return true;
    }

    private static final class FaceView implements MutableQuadView {
        private final Direction face;

        private FaceView(Direction face) {
            this.face = face == null ? Direction.NORTH : face;
        }

        @Override
        public Direction nominalFace() {
            return face;
        }
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
