/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.client.screen.plc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.neep.neepmeat.NeepMeat;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.render.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;

public class MonoTextRenderer
{
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/font/ascii-mono.png");

    private final StringSplitter handler = new StringSplitter((codePoint, style) -> charStride());

    public void drawTexture(Matrix4f matrix, float x, float y, float u, float v, float width, float height)
    {
        drawTexture(matrix, x, y, 0, u, v, width, height, 128, 128);
    }

    public static void drawTexture(Matrix4f matrix, float x, float y, float z, float u, float v, float width, float height, float textureWidth, float textureHeight)
    {
        drawTexture(matrix, x, x + width, y, y + height, z, width, height, u, v, textureWidth, textureHeight);
    }

    private static void drawTexture(Matrix4f matrix, float x0, float x1, float y0, float y1, float z, float regionWidth, float regionHeight, float u, float v, float textureWidth, float textureHeight)
    {
        drawTexturedQuad(matrix, x0, x1, y0, y1, z, (u + 0.0F) / textureWidth, (u + regionWidth) / textureWidth, (v + 0.0F) / textureHeight, (v + regionHeight) / textureHeight);
    }

    private static void drawTexturedQuad(Matrix4f matrix, float x0, float x1, float y0, float y1, float z, float u0, float u1, float v0, float v1)
    {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix, x0, y1, z).uv(u0, v1);
        bufferBuilder.vertex(matrix, x1, y1, z).uv(u1, v1);
        bufferBuilder.vertex(matrix, x1, y0, z).uv(u1, v0);
        bufferBuilder.vertex(matrix, x0, y0, z).uv(u0, v0);
    }

    public int fontHeight()
    {
        return 9;
    }

    public float draw(PoseStack matrices, @Nullable String text, float x, float y, int color)
    {
        if (text == null || text.isEmpty())
            return 0;

        return this.drawLayer(text, x, y, color, matrices.last().pose());
    }

    public float draw(PoseStack matrices, FormattedCharSequence text, float x, float y, int color)
    {
        return this.drawLayer(text, x, y, color, matrices.last().pose());
    }

    private void setTexture()
    {
//        TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
//        textureManager.getTexture(TEXTURE).setFilter(false, false);
//        RenderSystem.setShaderTexture(0, TEXTURE);
//
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
    }

    private float drawLayer(String text, float x, float y, int color, Matrix4f matrix)
    {
        setTexture();
        Drawer drawer = new Drawer(x, y, color, matrix);
        StringDecomposer.iterateFormatted(text, Style.EMPTY, drawer);
        return drawer.draw();
    }

    private float drawLayer(FormattedCharSequence text, float x, float y, int color, Matrix4f matrix)
    {
        setTexture();
        Drawer drawer = new Drawer(x, y, color, matrix);
        text.accept(drawer);
        return drawer.draw();
    }

    private int getU(int codePoint)
    {
        int rows = 16;
        int dx = 8;

        return codePoint % rows * dx;
    }

    private int getV(int codePoint)
    {
        int cols = 16;
        int dy = 8;

        return (codePoint / cols) * dy;
    }

    public float drawWithShadow(PoseStack matrices, String text, float x, float y, int color)
    {
        return this.draw(matrices, text, x, y, color);
    }

    public List<FormattedCharSequence> wrapLines(FormattedText text, int width)
    {
        return Language.getInstance().getVisualOrder(this.handler.splitLines(text, width, Style.EMPTY));
    }

    public void drawGlyph(int codePoint, float x, float y, Matrix4f matrix, BufferBuilder builder, boolean italic, float red, float green, float blue, float alpha, int light)
    {
        float x0 = x + 0;
        float x1 = x0 + charWidth();
        float y0 = y - 0;
        float y1 = y0 + charHeight();
        float h = 0 - 3.0F;
        float j = charHeight() - 3.0F;

        float u1 = getU(codePoint) / 128f;
        float v1 = getV(codePoint) / 128f;
        float u2 = (getU(codePoint) + charWidth()) / 128f;
        float v2 = (getV(codePoint) + charHeight()) / 128f;

        float m = italic ? 1.0F - 0.25F * h : 0.0F;
        float n = italic ? 1.0F - 0.25F * j : 0.0F;

        builder.vertex(matrix, x0, y1, 0).setColor(red, green, blue, alpha).uv(u1, v2);
        builder.vertex(matrix, x1, y1, 0).setColor(red, green, blue, alpha).uv(u2, v2);
        builder.vertex(matrix, x1, y0, 0).setColor(red, green, blue, alpha).uv(u2, v1);
        builder.vertex(matrix, x0, y0, 0).setColor(red, green, blue, alpha).uv(u1, v1);
    }

    public int getWidth(String text)
    {
        return text.length() * charStride();
//        return MathHelper.ceil(this.handler.getWidth(text));
    }

    public int charWidth()
    {
        return 5;
    }

    public int charStride()
    {
        return charWidth() + 1;
    }

    public int charHeight()
    {
        return 8;
    }

    public StringSplitter getTextHandler()
    {
        return handler;
    }

    public String trimToWidth(String text, int width)
    {
        return this.handler.plainHeadByWidth(text, width, Style.EMPTY);
    }

    private class Drawer implements FormattedCharSink
    {
        private final Matrix4f matrix;
        private final float r;
        private final float g;
        private final float b;
        private final float a;
        private final BufferBuilder bufferBuilder;
        private float x;
        private float y;

        public Drawer(float x, float y, int col, Matrix4f matrix)
        {
            this.bufferBuilder = Tesselator.getInstance().getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

            this.x = x;
            this.y = y;
            this.matrix = matrix;

            this.r = (col >> 16 & 255) / 255.0F;
            this.g = (col >> 8 & 255) / 255.0F;
            this.b = (col & 255) / 255.0F;
            this.a = (col >> 24 & 255) / 255.0F;
        }

        @Override
        public boolean accept(int index, Style style, int codePoint)
        {
            int width = charStride();

            drawGlyph(codePoint, x, y, matrix, bufferBuilder, false, r, g, b, a, 15728880);
            this.x += width;
            return true;
        }

        public float draw()
        {
            BufferUploader.drawWithShader(bufferBuilder.end());
            return x + 1;
        }
    }
}
