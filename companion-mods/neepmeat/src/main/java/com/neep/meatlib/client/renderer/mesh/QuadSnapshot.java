/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Emissive lightmaps are not stored.
 */

package com.neep.meatlib.client.renderer.mesh;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;

/**
 * Editable copy of one emitted quad. {@code pushTransform} mutates this, then it is baked again.
 */
public final class QuadSnapshot implements MutableQuadView {
    private Direction face;
    private final float[] x = new float[4];
    private final float[] y = new float[4];
    private final float[] z = new float[4];
    private final float[] u = new float[4];
    private final float[] v = new float[4];
    private final int[] color = new int[4];
    private final float[] nx = new float[4];
    private final float[] ny = new float[4];
    private final float[] nz = new float[4];
    private final TextureAtlasSprite sprite;

    private QuadSnapshot(Direction face, TextureAtlasSprite sprite) {
        this.face = face;
        this.sprite = sprite;
    }

    public static QuadSnapshot capture(Direction face, TextureAtlasSprite sprite, float[] x, float[] y, float[] z, float[] u, float[] v, int[] color, float[] nx, float[] ny, float[] nz) {
        QuadSnapshot snapshot = new QuadSnapshot(face, sprite);
        System.arraycopy(x, 0, snapshot.x, 0, 4);
        System.arraycopy(y, 0, snapshot.y, 0, 4);
        System.arraycopy(z, 0, snapshot.z, 0, 4);
        System.arraycopy(u, 0, snapshot.u, 0, 4);
        System.arraycopy(v, 0, snapshot.v, 0, 4);
        System.arraycopy(color, 0, snapshot.color, 0, 4);
        System.arraycopy(nx, 0, snapshot.nx, 0, 4);
        System.arraycopy(ny, 0, snapshot.ny, 0, 4);
        System.arraycopy(nz, 0, snapshot.nz, 0, 4);
        return snapshot;
    }

    public QuadSnapshot copy() {
        return capture(face, sprite, x, y, z, u, v, color, nx, ny, nz);
    }

    @Override
    public Direction nominalFace() {
        return face;
    }

    public QuadSnapshot pos(int index, float px, float py, float pz) {
        x[index] = px;
        y[index] = py;
        z[index] = pz;
        return this;
    }

    public QuadSnapshot uv(int index, float pu, float pv) {
        u[index] = pu;
        v[index] = pv;
        return this;
    }

    public BakedQuad bake() {
        if (sprite == null) {
            return new BakedQuad(new int[32], -1, face == null ? Direction.NORTH : face, null, false);
        }
        QuadBakingVertexConsumer consumer = new QuadBakingVertexConsumer();
        consumer.setSprite(sprite);
        consumer.setDirection(face == null ? Direction.NORTH : face);
        consumer.setShade(true);
        for (int i = 0; i < 4; i++) {
            int argb = color[i];
            consumer.addVertex(x[i], y[i], z[i]);
            consumer.setColor((argb >> 16) & 255, (argb >> 8) & 255, argb & 255, (argb >> 24) & 255);
            consumer.setUv(u[i], v[i]);
            consumer.setNormal(nx[i], ny[i], nz[i]);
        }
        return consumer.bakeQuad();
    }
}
