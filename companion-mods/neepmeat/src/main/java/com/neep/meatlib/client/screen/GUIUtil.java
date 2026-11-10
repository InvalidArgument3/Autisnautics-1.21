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

package com.neep.meatlib.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.neep.meatlib.MeatLib;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.*;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public interface GUIUtil
{
    ResourceLocation INVENTORY_BACKGROUND = ResourceLocation.fromNamespaceAndPath(MeatLib.ID, "textures/gui/inventory_background.png");
    ResourceLocation SCREEN_BORDER = ResourceLocation.fromNamespaceAndPath(MeatLib.ID, "textures/gui/screen_border.png");
    ResourceLocation WIDGETS = ResourceLocation.fromNamespaceAndPath(MeatLib.ID, "textures/gui/widget/widgets.png");

    static void drawTexture(ResourceLocation texture, GuiGraphics context, int x, int y, int u, int v, int width, int height)
    {
        drawTexture(texture, context, x, y, 0, (float) u, (float) v, width, height, 256, 256, 1, 1, 1, 1);
    }

    static void drawTexture(ResourceLocation texture, GuiGraphics context, int x, int y, int u, int v, int width, int height, int col)
    {
        float a = ((col >> 24) & 0xff) / 255f;
        float r = ((col >> 16) & 0xff) / 255f;
        float g = ((col >> 8) & 0xff) / 255f;
        float b = ((col) & 0xff) / 255f;
        drawTexture(texture, context, x, y, 0, (float) u, (float) v, width, height, 256, 256, r, g, b, a);
    }

    static void drawTexture(ResourceLocation texture, GuiGraphics context, int x, int y, int u, int v, int width, int height, float r, float g, float b, float a)
    {
        drawTexture(texture, context, x, y, 0, (float) u, (float) v, width, height, 256, 256, r, g, b, a);
    }

    static void drawTexture(ResourceLocation texture, GuiGraphics context, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight)
    {
        drawTexture(texture, context, x, x + width, y, y + height, 0, width, height, u, v, textureWidth, textureHeight, 1, 1, 1, 1);
    }

    static void drawTexture(ResourceLocation texture, GuiGraphics context, int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight, float r, float g, float b, float a)
    {
        drawTexture(texture, context, x, x + width, y, y + height, z, width, height, u, v, textureWidth, textureHeight, r, g, b, a);
    }

    static void drawTextureStretch(ResourceLocation texture, GuiGraphics context, int x1, int y1, int w, int h, float u, float v, int du, int dv, int textureWidth, int textureHeight)
    {
        innerBlit(texture, context, x1, x1 + w, y1, y1 + h, 0, u / textureWidth, (u + du) / textureWidth, v / textureHeight, (v + dv) / textureHeight, 1, 1, 1, 1);
    }

    static void drawTexture(ResourceLocation texture, GuiGraphics context, int x1, int x2, int y1, int y2, int z, int du, int dv, float u, float v, int textureWidth, int textureHeight, float r, float g, float b, float a)
    {
        innerBlit(texture, context, x1, x2, y1, y2, z, u / textureWidth, (u + du) / textureWidth, v / textureHeight, (v + dv) / textureHeight, r, g, b, a);
    }

    private static void innerBlit(ResourceLocation texture, GuiGraphics context, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, float r, float g, float b, float a)
    {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        Matrix4f matrix4f = context.pose().last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.addVertex(matrix4f, x1, y1, z).setColor(r, g, b, a).setUv(u1, v1);
        bufferBuilder.addVertex(matrix4f, x1, y2, z).setColor(r, g, b, a).setUv(u1, v2);
        bufferBuilder.addVertex(matrix4f, x2, y2, z).setColor(r, g, b, a).setUv(u2, v2);
        bufferBuilder.addVertex(matrix4f, x2, y1, z).setColor(r, g, b, a).setUv(u2, v1);
        BufferUploader.drawWithShader(bufferBuilder.build());
        RenderSystem.disableBlend();
    }

    static void renderFrameGradient(GuiGraphics context, int x, int y, int dx, int dy, int col, int offset)
    {
        drawHorizontalLine1(context, x - offset, x + dx + offset, y - offset, col);
        drawVerticalLine1(context, x - offset, y - offset, y + dy + offset, col);
        drawHorizontalLine1(context, x - offset, x + dx + offset, y + dy + offset, col);
        drawVerticalLine1(context, x + dx + offset, y - offset, y + dy + offset, col);
    }

    static void renderBorderL(GuiGraphics context, int x, int y, int dx, int dy, int col, int offset)
    {
        drawHorizontalLine1(context, x - offset, x + dx + offset - 1, y - offset, col);
        drawVerticalLine1(context, x - offset, y - offset, y + dy + offset, col);
        drawHorizontalLine1(context, x - offset + 1, x + dx + offset, y + dy + offset, col);
        drawVerticalLine1(context, x + dx + offset, y - offset, y + dy + offset, col);

        // bottom left
        fill(context, x - offset + 0.5f, y + offset + dy + 0.5f, x - offset + 1.5f, y + dy + offset - 0.5f, col);

        // Top right
        fill(context, x + dx + offset - 0.5f, y - offset + 0.5f, x + dx + offset + 0.5f, y - offset + 1.5f, col);
    }

    static void renderBorderInner(GuiGraphics context, Rectangle rectangle, int col, int offset)
    {
        renderFrameGradient(context, rectangle.x0(), rectangle.y0(), rectangle.nw() - 1, rectangle.nh() - 1, col, offset);
    }

    static void renderBorder(GuiGraphics context, int x, int y, int dx, int dy, int col, int offset)
    {
        renderBorderInner(context, x, y, dx, dy, col, offset);
    }

    static void renderBorderInner(GuiGraphics context, int x, int y, int dx, int dy, int col, int offset)
    {
        renderFrameGradient(context, x, y, dx - 1, dy - 1, col, offset);
    }

    static void drawCenteredText(GuiGraphics context, Font textRenderer, Component text, float centerX, float y, int color, boolean shadow)
    {
//        FormattedCharSequence orderedText = text.asOrderedText();
        drawText(context, textRenderer, text, centerX - textRenderer.width(text) / 2f, y, color, shadow);
    }


    static int drawText(GuiGraphics context, Font textRenderer, Component text, float x, float y, int color, boolean shadow)
    {
        return context.drawString(textRenderer, text, (int) x, (int) y, color, shadow);
    }

    static int drawText(GuiGraphics context, Font textRenderer, FormattedCharSequence text, float x, float y, int color, boolean shadow)
    {
        int level = 3;
        float f = (1f / level) * (float) Minecraft.getInstance().getWindow().getGuiScale();
        var matrices = context.pose();
        matrices.pushPose();
//        matrices.scale(1f / f, 1f / f, 1);

        int i = context.drawString(textRenderer, text, x, y, color, shadow);
        matrices.popPose();
        return i;
    }

    static int drawText(GuiGraphics context, Font textRenderer, String text, float x, float y, int color, boolean shadow)
    {
        return context.drawString(textRenderer, text, x, y, color, shadow);
    }

    static void drawHorizontalLine1(GuiGraphics context, int x1, int x2, int y, int color)
    {
        if (x2 < x1)
        {
            int i = x1;
            x1 = x2;
            x2 = i;
        }
        context.fill(x1, y, x2 + 1, y + 1, color);
    }

    static void drawVerticalLine1(GuiGraphics context, float x, float y1, float y2, int color)
    {
        if (y2 < y1)
        {
            float i = y1;
            y1 = y2;
            y2 = i;
        }
        fill(context, x, y1 + 1, x + 1, y2, color);
    }

    static void drawCentralVerticalLine(GuiGraphics context, float x, float y1, float y2, float radius, int color)
    {
        if (y2 < y1)
        {
            float i = y1;
            y1 = y2;
            y2 = i;
        }
        fill(context, x - radius, y1 + 1, x + radius, y2, color);
    }

    static void drawCentralHorizontalLine(GuiGraphics context, float x1, float x2, float y, float radius, int color)
    {
        if (x2 < x1)
        {
            float i = x1;
            x1 = x2;
            x2 = i;
        }
        fill(context, x1, y - radius, x2, y + radius, color);
    }

    static void drawInventoryBackground(GuiGraphics context, int x, int y)
    {
        context.blit(INVENTORY_BACKGROUND, x, y, 0, 0, 176, 90);
    }

    static void drawScreenBorder(GuiGraphics context, int x, int y, int w, int h)
    {
        blitNineSliced(context, SCREEN_BORDER, x, y, w, h, 5, 256, 55, 0, 0);
    }

    static void drawHashedScreenBorder(GuiGraphics context, int x, int y, int w, int h)
    {
        blitNineSliced(context, SCREEN_BORDER, x, y, w, h, 4, 256, 54, 0, 55);
    }

    /**
     * Segments and (sometimes) stretches the defined region to draw it as a window border. Stretching occurs when the desired size exceeds that of the image segment.
     */
    static void blitNineSliced(GuiGraphics matrices, ResourceLocation texture, int x, int y, int width, int height, int sliceBorder, int regionWidth, int regionHeight, int u, int v)
    {
        int z = 0;
        int centreSliceWidth = regionWidth - 2 * sliceBorder;
        int centreWidth = width - 2 * sliceBorder;
        int centreSliceHeight = regionHeight - 2 * sliceBorder;

        int topAndBottomU1 = u + sliceBorder + Math.min(centreSliceWidth, width - 2 * sliceBorder);

        // Top
        innerBlit(matrices, texture, x + sliceBorder, x + width - sliceBorder, y, y + sliceBorder, z,
                u + sliceBorder, topAndBottomU1,
                v, v + sliceBorder);

        // Bottom
        innerBlit(matrices, texture, x + sliceBorder, x + width - sliceBorder, y + height - sliceBorder, y + height, z,
                u + sliceBorder, topAndBottomU1,
                v + regionHeight - sliceBorder, v + regionHeight);

        int leftRightV1 = v + sliceBorder + Math.min(width - 2 * sliceBorder, regionHeight - 2 * sliceBorder);

        // Left
        innerBlit(matrices, texture, x, x + sliceBorder, y + sliceBorder, y + height - sliceBorder, z,
                u, u + sliceBorder,
                v + sliceBorder, leftRightV1);

        // Right
        innerBlit(matrices, texture, x + width - sliceBorder, x + width, y + sliceBorder, y + height - sliceBorder, z,
                u + regionWidth - sliceBorder, u + regionWidth,
                v + sliceBorder, leftRightV1);

        // Middle
        int i = 0;
        for (int remainingX = width - 2 * sliceBorder; remainingX > 0; remainingX -= centreSliceWidth)
        {
            int drawWidth = Math.min(remainingX, centreSliceWidth);

            int j = 0;
            for (int remainingY = height - 2 * sliceBorder; remainingY > 0; remainingY -= centreSliceHeight)
            {
                int drawHeight = Math.min(remainingY, centreSliceHeight);
                innerBlit(matrices, texture,
                        x + sliceBorder + i * centreSliceWidth, x + sliceBorder + i * centreSliceWidth + drawWidth,
                        y + sliceBorder + j * centreSliceHeight, y + sliceBorder + j * centreSliceHeight + drawHeight,
                        z,
                        u + sliceBorder, u + drawWidth,
                        v + sliceBorder, v + drawHeight);
                ++j;
            }
            ++i;
        }

        // Top left corner
        innerBlit(matrices, texture, x, x + sliceBorder, y, y + sliceBorder, z, u, u + sliceBorder, v, v + sliceBorder);

        // Bottom left corner
        innerBlit(matrices, texture, x, x + sliceBorder, y + height - sliceBorder, y + height, z, u, u + sliceBorder, v + regionHeight - sliceBorder, v + regionHeight);

        // Bottom right corner
        innerBlit(matrices, texture, x + width - sliceBorder, x + width, y + height - sliceBorder, y + height, z, u + regionWidth - sliceBorder, u + regionWidth, v + regionHeight - sliceBorder, v + regionHeight);

        // Top right corner
        innerBlit(matrices, texture, x + width - sliceBorder, x + width, y, y + sliceBorder, z, u + regionWidth - sliceBorder, u + regionWidth, v, v + sliceBorder);
    }

    static void innerBlit(GuiGraphics context, ResourceLocation texture, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1)
    {
        u0 /= 256;
        u1 /= 256;
        v0 /= 256;
        v1 /= 256;
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix = context.pose().last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.addVertex(matrix, x0, y1, z).setUv(u0, v1);
        bufferBuilder.addVertex(matrix, x1, y1, z).setUv(u1, v1);
        bufferBuilder.addVertex(matrix, x1, y0, z).setUv(u1, v0);
        bufferBuilder.addVertex(matrix, x0, y0, z).setUv(u0, v0);
        BufferUploader.drawWithShader(bufferBuilder.build());
    }

    static void fill(GuiGraphics context, float x1, float y1, float x2, float y2, int col)
    {
        context.fill(Math.round(Math.min(x1, x2)), Math.round(Math.min(y1, y2)),
                Math.round(Math.max(x1, x2)), Math.round(Math.max(y1, y2)), col);
    }
}
