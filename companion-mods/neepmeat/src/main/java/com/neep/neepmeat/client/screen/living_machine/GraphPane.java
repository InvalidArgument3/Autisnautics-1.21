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

package com.neep.neepmeat.client.screen.living_machine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.neepmeat.api.live_machine.metrics.DataLog;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.client.screen.util.Border;
import com.neep.neepmeat.screen_handler.LivingMachineScreenHandler;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import org.joml.Matrix4f;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class GraphPane extends LivingMachineScreen.PaneWidget
{
    private Rectangle windowBounds;
    private Rectangle plotBounds;

    private final Font textRenderer = Minecraft.getInstance().font;
    private final LivingMachineScreenHandler handler;
    private DataLog.DataView dataView = DataLog.DataView.EMPTY;

    private final DecimalFormat yFormat = new DecimalFormat("###.#%");
    private final DecimalFormat timeFormat = new DecimalFormat("###.#");

    public GraphPane(LivingMachineScreenHandler handler)
    {
        this.handler = handler;
    }

    public void tick()
    {

    }

    @Override
    public void init()
    {
        super.init();
        int scaleWidth = 20;

        this.windowBounds = new Rectangle.ImmutableRect(x0() + scaleWidth, y0() + 10,
                nw() - 3 * 10, nh() - 2 * 10);
        int padding = 4;
        this.plotBounds = new Rectangle.ImmutableRect(windowBounds.x0() + padding, windowBounds.y0() + padding, windowBounds.nw() - 2 * padding,  windowBounds.nh() - 2 * padding - 1);
        this.border = new Border(windowBounds, 0, () -> PLCCols.BORDER.col);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);

        long[] time = dataView.time();

        long period = dataView.capacity() * 40L;
        long timeStart = time.length > 0 ? time[0] : 0;
        long timeEnd = timeStart + period;

        long endTime = drawCurve(context, time, dataView.efficiency(), timeStart, timeEnd, PLCCols.SELECTED.col);
        drawCurve(context, time, dataView.health(), timeStart, timeEnd, PLCCols.BORDER.col);

        drawYScale(context, 0, 1, yFormat);
        drawTimeScale(context, 0, period, endTime, timeFormat);
        drawLegend(context, dataView.getLegend());
    }

    private long drawCurve(GuiGraphics context, long[] time, double[] yVals, long timeStart, long timeEnd, int col)
    {
        long period = timeEnd - timeStart;

        long highestTime = 0;
        for (int i = 0; i < time.length; ++i)
        {
            if (i % 2 == 0)
                continue;

            long t1 = time[i];

            if (t1 >= timeStart && t1 < timeEnd && i + 1 < time.length)
            {
                long t2 = time[i + 1];

                if (t2 <= timeStart || t2 > timeEnd)
                    break;

                highestTime = t2 - timeStart;

                float y1 = (float) yVals[i];
                float y2 = (float) yVals[i + 1];

//                float period = timeEnd - timeStart;
                float xStride = (float) plotBounds.nw();
                float yStride = plotBounds.nh();
                float t1Scaled = plotBounds.x0() + (float) (t1 - timeStart) / period * xStride;
                float t2Scaled = plotBounds.x0() + (float) (t2 - timeStart) / period * xStride;

                float y1Scaled = plotBounds.y0() + plotBounds.nh() - y1 * yStride;
                float y2Scaled = plotBounds.y0() + plotBounds.nh() - y2 * yStride;

                drawLine(context, t1Scaled, y1Scaled, t2Scaled, y2Scaled, 0.5f, col);
            }
        }
        return highestTime;
    }

    private void drawLegend(GuiGraphics context, List<ObjectIntPair<Component>> entries)
    {
//        float entryPadding = 5;
//        float totalWidth = 0;
//        for (var entry : entries)
//        {
//            totalWidth += textRenderer.getWidth(entry.key()) + entryPadding;
//        }

        PoseStack matrices = context.pose();
        float scale = 0.7f;
        for (int i = 0; i < entries.size(); ++i)
        {
            var entry = entries.get(i);

            Component name = entry.left();
            int col = entry.rightInt();

            int squareWidth = (int) (textRenderer.lineHeight * scale);
            float legendWidth = textRenderer.width(name) * scale + squareWidth + 1;
            float y = (windowBounds.y0() - textRenderer.lineHeight * scale);
            float x = plotBounds.x0() + (i + 0.5f) * ((float) plotBounds.nw() / (entries.size())) - legendWidth / 2;

            context.fill((int) x, (int) y, (int) x + squareWidth, (int) y + squareWidth, col);
            matrices.pushPose();
            matrices.translate(x + squareWidth + 1, y, 0);
            matrices.scale(scale, scale, 1);
            GUIUtil.drawText(context, textRenderer, name, 0, 0, PLCCols.SELECTED.col, false);
            matrices.popPose();
        }
    }

    private void drawTimeScale(GuiGraphics context, long lower, long upper, long zero, DecimalFormat format)
    {
        PoseStack matrices = context.pose();
        long divisions = 5;

        long period = upper - lower;
        long stride = period / divisions;

        for (int i = 0; i < divisions + 1; ++i)
        {
            long value = i * stride - zero;

            String formatted = formatTime(value);

            float y = plotBounds.y0() + plotBounds.nh();
            float x = plotBounds.x0() + (float) (i * stride) / period * plotBounds.nw();

            matrices.pushPose();
            matrices.translate(x, (y + ((float) textRenderer.lineHeight / 2f)) + 2, 0);
            matrices.scale(0.7f, 0.7f, 1);
            GUIUtil.drawText(context, textRenderer, formatted, - textRenderer.width(formatted) / 2f, 0,
                    PLCCols.SELECTED.col, false);
            matrices.popPose();
            int tickLength = 6; // For some reason, this length has to be 1 higher than its horizontal counterpart.
            GUIUtil.drawVerticalLine1(context, (int) x, (int) y, (int) (y + tickLength), PLCCols.SELECTED.col);
        }
    }

    private static String formatTime(long ticks)
    {
        long secs = ticks / 20;

        Duration duration = Duration.ofSeconds(secs);
        if (duration.toHoursPart() != 0)
        {
            return duration.toHoursPart() + "h " + duration.toMinutesPart() + "m";
        }
        else if (duration.toMinutesPart() != 0)
        {
            return duration.toMinutesPart() + "m " + duration.toSecondsPart() + "s";
        }
        else
        {
            return duration.toSecondsPart() + "s";
        }
    }

    private void drawYScale(GuiGraphics context, float lower, float upper, DecimalFormat format)
    {
        PoseStack matrices = context.pose();
        int divisions = 10;

        float stride = (upper - lower) / divisions;

        for (int i = 0; i < divisions + 1; ++i)
        {
            float value = i * stride;

            String formatted = format.format(value);
            float y = (plotBounds.y0() + plotBounds.nh() - (i * stride) / (upper - lower) * plotBounds.nh());
            matrices.pushPose();
            matrices.translate(x0(), (y - ((float) textRenderer.lineHeight / 2f)) + 2, 0);
            matrices.scale(0.7f, 0.7f, 1);
            GUIUtil.drawText(context, textRenderer, formatted, 0, 0,
                    PLCCols.SELECTED.col, false);
            matrices.popPose();
            int tickLength = 5;
            GUIUtil.drawHorizontalLine1(context, plotBounds.x0() - tickLength, plotBounds.x0(), (int) y, PLCCols.SELECTED.col);
        }
    }

    public void update(DataLog.DataView dataView)
    {
        this.dataView = dataView;
    }

    public static void drawLine(GuiGraphics context, float x1, float y1, float x2, float y2, float radius, int col)
    {
        float z = 10;
        double angle = Mth.atan2((y2 - y1), (x2 - x1));
        float l = Mth.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));

        float f = (float) FastColor.ARGB32.alpha(col) / 255.0F;
        float g = (float) FastColor.ARGB32.red(col) / 255.0F;
        float h = (float) FastColor.ARGB32.green(col) / 255.0F;
        float j = (float) FastColor.ARGB32.blue(col) / 255.0F;

        Matrix4f matrix4f = new Matrix4f()
                .identity()
                .translate(x1, y1, 0)
                .rotateZ((float) angle);

        VertexConsumer vertexConsumer = context.bufferSource().getBuffer(RenderType.gui());
        vertexConsumer.addVertex(matrix4f, 0, - radius, z).setColor(g, h, j, f);
        vertexConsumer.addVertex(matrix4f, 0, + radius, z).setColor(g, h, j, f);
        vertexConsumer.addVertex(matrix4f, l, + radius, z).setColor(g, h, j, f);
        vertexConsumer.addVertex(matrix4f, l, - radius, z).setColor(g, h, j, f);
    }
}
