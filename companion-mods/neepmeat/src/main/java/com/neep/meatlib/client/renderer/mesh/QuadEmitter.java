/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * square() plus spriteBake() becomes one BakedQuad. Other vertex edits are ignored.
 */

package com.neep.meatlib.client.renderer.mesh;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;

import java.util.ArrayList;
import java.util.List;

public class QuadEmitter implements MutableQuadView {
    private final List<BakedQuad> quads = new ArrayList<>();
    private Direction face = Direction.NORTH;
    private TextureAtlasSprite sprite;

    public QuadEmitter square(Direction direction, float left, float bottom, float right, float top, float depth) {
        this.face = direction;
        return this;
    }

    public QuadEmitter nominalFace(Direction direction) {
        this.face = direction;
        return this;
    }

    public QuadEmitter spriteBake(int spriteIndex, TextureAtlasSprite sprite, int bakeFlags) {
        this.sprite = sprite;
        return this;
    }

    public QuadEmitter spriteColor(int spriteIndex, int c0, int c1, int c2, int c3) {
        return this;
    }

    public QuadEmitter emit() {
        if (sprite == null || face == null) {
            return this;
        }
        QuadBakingVertexConsumer consumer = new QuadBakingVertexConsumer();
        consumer.setSprite(sprite);
        consumer.setDirection(face);
        consumer.setShade(true);
        float min = 0;
        float max = 16;
        consumer.addVertex(min, min, min);
        consumer.addVertex(min, max, min);
        consumer.addVertex(max, max, min);
        consumer.addVertex(max, min, min);
        quads.add(consumer.bakeQuad());
        return this;
    }

    public List<BakedQuad> baked() {
        return List.copyOf(quads);
    }
}
