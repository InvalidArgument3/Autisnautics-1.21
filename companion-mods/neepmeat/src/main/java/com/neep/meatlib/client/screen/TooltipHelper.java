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
import com.neep.meatlib.theme.PLCCols;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.joml.Matrix4f;

import java.util.List;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class TooltipHelper
{
    private static final TooltipHelper INSTANCE = new TooltipHelper(true, PLCCols.TEXT.col, false);

    public boolean offset;
    public int col;
    public boolean forceLeft;

    private final Font textRenderer;
    private final Minecraft client = Minecraft.getInstance();

    public TooltipHelper(boolean offset, int col, boolean forceLeft, Font textRenderer)
    {
        this.offset = offset;
        this.col = col;
        this.forceLeft = forceLeft;
        this.textRenderer = textRenderer;
    }

    public TooltipHelper(boolean offset, int col, boolean forceLeft)
    {
        this(offset, col, forceLeft, Minecraft.getInstance().font);
    }

    public static TooltipHelper get()
    {
        return INSTANCE;
    }

    public static List<Component> wrapLines(List<Component> list, Component text)
    {
        for (FormattedCharSequence line : Minecraft.getInstance().font.split(text, 200))
        {
            MutableComponent textLine = Component.empty();
            line.accept((index, style, codePoint) ->
            {
                textLine.append(Component.literal(Character.toString(codePoint)).setStyle(style));
                return true;
            });
            list.add(textLine);
        }
        return list;
    }

    public void draw(GuiGraphics context, int x, int y, List<Component> texts)
    {
        renderTooltipComponents(context,
                texts.stream().map(t -> ClientTooltipComponent.create(t.getVisualOrderText())).collect(Collectors.toList()),
                offset,
                x,
                y,
                0,
                col,
                forceLeft);
    }

    public void drawOrdered(GuiGraphics context, int x, int y, List<FormattedCharSequence> texts, int maxWidth)
    {
        renderTooltipComponents(context,
                texts.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()),
                offset,
                x,
                y,
                maxWidth,
                col,
                forceLeft);
    }

    public void drawOrdered(GuiGraphics context, int x, int y, List<FormattedCharSequence> texts)
    {
        drawOrdered(context, x, y, texts, 0);
    }

    public void renderTooltipComponents(GuiGraphics context, List<ClientTooltipComponent> components, boolean offset, int x, int y, int maxWidth, int col, boolean forceLeft)
    {
        PoseStack matrices = context.pose();
        if (offset)
        {
            x += 12;
            y -= 12;
        }

        if (components.isEmpty())
        {
            return;
        }

        int maxHeight = components.size() == 1 ? -2 : 0;
        for (ClientTooltipComponent tooltipComponent : components)
        {
            int componentWidth = tooltipComponent.getWidth(textRenderer);
            if (componentWidth > maxWidth)
            {
                maxWidth = componentWidth;
            }
            maxHeight += tooltipComponent.getHeight();
        }

        if (forceLeft)
        {
            x -= maxWidth + (offset ? 4 : 2);
        }

        if (x + maxWidth > width())
        {
            x -= maxWidth + (offset ? 28 : 2);
        }

        if (y + maxHeight + 3 > height())
        {
            y = height() - maxHeight - 3;
        }

        if (y < 0)
            y = 0;

        if (x < 0)
            x = 0;

        matrices.pushPose();
//        this.itemRenderer.zOffset = 400.0f;
//        this.setZOffset(400);
        matrices.translate(0, 0, 400);
        context.fill(x, y, x + maxWidth + 2, y + maxHeight + 2, 0xff000000);
        GUIUtil.drawHorizontalLine1(context, x, x + maxWidth + 2, y, col);
        GUIUtil.drawHorizontalLine1(context, x, x + maxWidth + 2, y + maxHeight + 2, col);
        GUIUtil.drawVerticalLine1(context, x + maxWidth + 2, y, y + maxHeight + 2, col);
        GUIUtil.drawVerticalLine1(context, x, y, y + maxHeight + 2, col);

        RenderSystem.disableBlend();
        com.mojang.blaze3d.vertex.ByteBufferBuilder bufferBuilder = new com.mojang.blaze3d.vertex.ByteBufferBuilder(1536);
        MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(bufferBuilder);
        matrices.translate(0.0, 0.0, 400.0);

        Matrix4f matrix4f = matrices.last().pose();
        int yAdvance = y + 2;

        for (ClientTooltipComponent tooltipComponent2 : components)
        {
            tooltipComponent2.renderText(textRenderer, x + 2, yAdvance, matrix4f, immediate);
            yAdvance += tooltipComponent2.getHeight();
        }

        immediate.endBatch();
        bufferBuilder.close();
        matrices.popPose();
        yAdvance = y;
        for (int index = 0; index < components.size(); ++index)
        {
            ClientTooltipComponent tooltipComponent2 = components.get(index);
            tooltipComponent2.renderImage(textRenderer, x, yAdvance, context);
            yAdvance += tooltipComponent2.getHeight() + (index == 0 ? 2 : 0);
        }
    }

    private int width()
    {
        return client.getWindow().getGuiScaledWidth();
    }

    private int height()
    {
        return client.getWindow().getGuiScaledHeight();
    }
}
