/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Emissive lightmaps are not stored. Vertex position, UV, colour, and normal are.
 */

package com.neep.meatlib.client.renderer.mesh;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class QuadEmitter implements MutableQuadView {
    private final List<BakedQuad> quads = new ArrayList<>();
    private final float[] x = new float[4];
    private final float[] y = new float[4];
    private final float[] z = new float[4];
    private final float[] u = new float[4];
    private final float[] v = new float[4];
    private final int[] color = {-1, -1, -1, -1};
    private final float[] nx = new float[4];
    private final float[] ny = new float[4];
    private final float[] nz = new float[4];
    private Direction face = Direction.NORTH;
    private TextureAtlasSprite sprite;
    private boolean normalsSet;

    public QuadEmitter square(Direction direction, float left, float bottom, float right, float top, float depth) {
        if (Math.abs(depth) < 0.0001f) {
            depth = 0;
        } else if (Math.abs(depth - 1) < 0.0001f) {
            depth = 1;
        }
        switch (direction) {
            case UP -> {
                depth = 1 - depth;
                top = 1 - top;
                bottom = 1 - bottom;
                pos(0, left, depth, bottom);
                pos(1, left, depth, top);
                pos(2, right, depth, top);
                pos(3, right, depth, bottom);
            }
            case DOWN -> {
                pos(0, left, depth, bottom);
                pos(1, left, depth, top);
                pos(2, right, depth, top);
                pos(3, right, depth, bottom);
            }
            case EAST -> {
                depth = 1 - depth;
                left = 1 - left;
                right = 1 - right;
                pos(0, depth, top, left);
                pos(1, depth, bottom, left);
                pos(2, depth, bottom, right);
                pos(3, depth, top, right);
            }
            case WEST -> {
                pos(0, depth, top, left);
                pos(1, depth, bottom, left);
                pos(2, depth, bottom, right);
                pos(3, depth, top, right);
            }
            case SOUTH -> {
                depth = 1 - depth;
                left = 1 - left;
                right = 1 - right;
                pos(0, 1 - left, top, depth);
                pos(1, 1 - left, bottom, depth);
                pos(2, 1 - right, bottom, depth);
                pos(3, 1 - right, top, depth);
            }
            case NORTH -> {
                pos(0, 1 - left, top, depth);
                pos(1, 1 - left, bottom, depth);
                pos(2, 1 - right, bottom, depth);
                pos(3, 1 - right, top, depth);
            }
        }
        nominalFace(direction);
        return this;
    }

    public QuadEmitter nominalFace(Direction direction) {
        this.face = direction == null ? Direction.NORTH : direction;
        float stepX = face.getStepX();
        float stepY = face.getStepY();
        float stepZ = face.getStepZ();
        for (int i = 0; i < 4; i++) {
            nx[i] = stepX;
            ny[i] = stepY;
            nz[i] = stepZ;
        }
        normalsSet = true;
        return this;
    }

    public QuadEmitter pos(int index, float px, float py, float pz) {
        x[index] = px;
        y[index] = py;
        z[index] = pz;
        return this;
    }

    public QuadEmitter pos(int index, Vector3f position) {
        return pos(index, position.x, position.y, position.z);
    }

    public QuadEmitter uv(int index, float pu, float pv) {
        u[index] = pu;
        v[index] = pv;
        return this;
    }

    public QuadEmitter spriteColor(int spriteIndex, int c0, int c1, int c2, int c3) {
        color[0] = c0;
        color[1] = c1;
        color[2] = c2;
        color[3] = c3;
        return this;
    }

    public QuadEmitter spriteBake(TextureAtlasSprite sprite, int bakeFlags) {
        return spriteBake(0, sprite, bakeFlags);
    }

    public QuadEmitter spriteBake(int spriteIndex, TextureAtlasSprite sprite, int bakeFlags) {
        this.sprite = sprite;
        if (sprite == null) {
            return this;
        }
        if ((bakeFlags & BAKE_LOCK_UV) != 0) {
            for (int i = 0; i < 4; i++) {
                float[] surface = surface(face, x[i], y[i], z[i]);
                uv(i, sprite.getU(surface[0] * 16f), sprite.getV(surface[1] * 16f));
            }
        } else {
            uv(0, sprite.getU0(), sprite.getV0());
            uv(1, sprite.getU0(), sprite.getV1());
            uv(2, sprite.getU1(), sprite.getV1());
            uv(3, sprite.getU1(), sprite.getV0());
        }
        return this;
    }

    public QuadEmitter emit() {
        if (sprite == null) {
            return this;
        }
        if (!normalsSet) {
            geometricNormal();
        }
        QuadBakingVertexConsumer consumer = new QuadBakingVertexConsumer();
        consumer.setSprite(sprite);
        consumer.setDirection(face);
        consumer.setShade(true);
        for (int i = 0; i < 4; i++) {
            int argb = color[i];
            consumer.addVertex(x[i], y[i], z[i]);
            consumer.setColor((argb >> 16) & 255, (argb >> 8) & 255, argb & 255, (argb >> 24) & 255);
            consumer.setUv(u[i], v[i]);
            consumer.setNormal(nx[i], ny[i], nz[i]);
        }
        quads.add(consumer.bakeQuad());
        return this;
    }

    public BakedQuad toBakedQuad(TextureAtlasSprite sprite) {
        this.sprite = sprite;
        int before = quads.size();
        emit();
        return quads.size() == before ? new BakedQuad(new int[32], -1, face, sprite, false) : quads.get(quads.size() - 1);
    }

    public BakedQuad toBakedQuad(int spriteIndex, TextureAtlasSprite sprite, boolean shade) {
        return toBakedQuad(sprite);
    }

    public List<BakedQuad> baked() {
        return List.copyOf(quads);
    }

    public void clearBaked() {
        quads.clear();
    }

    public void addBaked(List<BakedQuad> baked) {
        quads.addAll(baked);
    }

    private void geometricNormal() {
        float ax = x[1] - x[0];
        float ay = y[1] - y[0];
        float az = z[1] - z[0];
        float bx = x[2] - x[0];
        float by = y[2] - y[0];
        float bz = z[2] - z[0];
        float cx = ay * bz - az * by;
        float cy = az * bx - ax * bz;
        float cz = ax * by - ay * bx;
        float length = (float) Math.sqrt(cx * cx + cy * cy + cz * cz);
        if (length > 1.0e-6f) {
            cx /= length;
            cy /= length;
            cz /= length;
        }
        for (int i = 0; i < 4; i++) {
            nx[i] = cx;
            ny[i] = cy;
            nz[i] = cz;
        }
        normalsSet = true;
    }

    private static float[] surface(Direction face, float px, float py, float pz) {
        return switch (face) {
            case UP, DOWN -> new float[] {px, pz};
            case NORTH, SOUTH -> new float[] {px, 1 - py};
            case EAST, WEST -> new float[] {pz, 1 - py};
        };
    }
}
