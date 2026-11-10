package com.neep.neepmeat.client.screen.tablet.graph;

import com.neep.meatlib.client.RotationAxis;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.primitive.MutableRectangle;
import com.neep.meatlib.client.screen.primitive.Point;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.neepmeat.api.theme.GuideCols;
import com.neep.neepmeat.client.screen.living_machine.GraphPane;
import com.neep.neepmeat.guide.article.GraphContent;
import com.neep.neepmeat.util.NMMaths;
import it.unimi.dsi.fastutil.floats.FloatFloatPair;
import org.joml.Matrix4f;

import java.text.DecimalFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public class Graph
{
    private final GraphData data;
    private final Font textRenderer = Minecraft.getInstance().font;
    private final ResourceLocation unifont = ResourceLocation.tryBuild("minecraft", "uniform");
    private final Style style = Style.EMPTY.withFont(unifont);

    private final DecimalFormat format = new DecimalFormat("##.#");

    public Graph(GraphData data)
    {
        this.data = data;
    }

    public void render(GuiGraphics c, Rectangle bounds, float mx, float my)
    {
        var b = new MutableRectangle(bounds);
        var titleRect = b.shaveTop(10);
        titleRect.shaveLeft(25);
        var centre = titleRect.getCentre();
        GUIUtil.drawCenteredText(c, textRenderer, Component.literal(data.title()).setStyle(style), centre.x0(), titleRect.y0(), GuideCols.textCol(), false);
        renderLabels(c, b);

        var graphBounds = new MutableRectangle(b).grow(-1);
        GUIUtil.renderBorderInner(c, b, GuideCols.textCol(), 0);

        renderLines(c, graphBounds, true, mx, my);
        renderTicks(c, graphBounds);

        renderInteractive(c, graphBounds, mx, my);

    }

    // Converts the mx to local, samples graph with linear interpolation, displays guides and an extra tick
    private void renderInteractive(GuiGraphics c, Rectangle b, float mx, float my)
    {
        if (b.isWithin(mx, my))
        {
            var matrices = c.pose();

            float fx = (mx - b.x0()) / b.nw(); // [0 1]
            float sampleX = (float) data.xRange().unMap(data.semiLogX, fx); // Convert normalised coordinate to scale coordinate
            float sampleY = data.sample(sampleX);

//            System.out.println("S: " + sampleX + " V: " + data.sample((float) sampleX));

            // Guides
            GUIUtil.drawCentralVerticalLine(c, mx, b.y0(), b.y1(), 0.5f, GuideCols.darkCol());
            // Convert sampled Y back to screen coordinates
            var thing = (float) (b.y0() + (1 - data.yRange().map(false, sampleY)) * b.nh());
            GUIUtil.drawCentralHorizontalLine(c, b.x0(), b.x1(), thing, 0.5f, GuideCols.darkCol());

            float s = 1f;

            // Y
            {
                matrices.pushPose();
                String label = format.format(sampleY);
                Component text = Component.literal(label).setStyle(style);
                float tw = textRenderer.width(text) * s;
                matrices.translate(b.x0() - tw - 1, thing - textRenderer.lineHeight / 2f, 0);
                GUIUtil.drawText(c, textRenderer, text, 0, 0, GuideCols.textCol(), false);
                matrices.popPose();
            }

            // X
            {
                matrices.pushPose();
                String label = format.format(sampleX);
                Component text = Component.literal(label).setStyle(style);
                matrices.translate(mx, b.y1(), 0);
                GUIUtil.drawCenteredText(c, textRenderer, text, 0, 0, GuideCols.textCol(), false);
                matrices.popPose();
            }
        }
    }

    private void renderTicks(GuiGraphics c, MutableRectangle b)
    {
        var matrices = c.pose();

        float s = 1f;

        int tickCol = GuideCols.textCol();
        for (var tick : data.yTicks())
        {
            float yPos = b.y0() + b.nh() * (float) (1 - data.yRange().map(false, tick.value()));
            if (tick.value() != data.yRange().start())
                GraphPane.drawLine(c, b.x0(), yPos, b.x0() + 3, yPos, 0.5f, tickCol);

            matrices.pushPose();
            String label = tick.label().isEmpty() ? format.format(tick.value()) : tick.label();
            Component text = Component.literal(label).setStyle(style);
            float tw = textRenderer.width(text) * s;
            matrices.translate(b.x0() - tw - 1, yPos - textRenderer.lineHeight / 2f, 0);
            GUIUtil.drawText(c, textRenderer, text, 0, 0, GuideCols.textCol(), false);
            matrices.popPose();
        }

        for (var tick : data.xTicks())
        {
            float f = (float) (data.xRange().map(data.semiLogX, tick.value()));
            float xPos = b.x0() + b.nw() * f;

            // Skip if out of bounds
            if (f < 0 || f > 1)
                continue;

            if (tick.value() != data.xRange().start())
                GraphPane.drawLine(c, xPos, b.y1(), xPos, b.y1() - 3, 0.5f, tickCol);

            matrices.pushPose();
            String label = tick.label().isEmpty() ? format.format(tick.value()) : tick.label();
            Component text = Component.literal(label).setStyle(style);
            matrices.translate(xPos, b.y1(), 0);
            GUIUtil.drawCenteredText(c, textRenderer, text, 0, 0, GuideCols.textCol(), false);
            matrices.popPose();

            if (tick.fill())
                GUIUtil.drawCentralVerticalLine(c, xPos, b.y0(), b.y1(), 0.5f, GuideCols.darkCol());
        }
    }

    private void renderLabels(GuiGraphics c, MutableRectangle b)
    {
        int yw = 28;
        int xw = 20;
        var yLabel = b.shaveLeft(yw);
        Rectangle xLabel = b.shaveBottom(xw);
        yLabel.shaveBottom(xw);

        // Y
        var matrices = c.pose();
        matrices.pushPose();
        Matrix4f m = new Matrix4f();
        float s = 1f;
        var cen = yLabel.getCentre();
        cen = new Point.ImmutablePoint(yLabel.x0(), cen.y0());
        m.rotateAroundLocal(RotationAxis.POSITIVE_Z.rotationDegrees(-90), cen.x0(), cen.y0(), 0);
        m.scaleAroundLocal(s, s, 1, cen.x0(), cen.y0(), 0);
        m.translate(cen.x0(), cen.y0(), 0);
        matrices.mulPoseMatrix(m);

        renderLabel(c, data.yLabel());
        matrices.popPose();

//        GUIUtil.renderBorderInner(c, yLabel, 0xffff00ff, 0); // debug

        // X
        matrices.pushPose();
        cen = xLabel.getCentre();
        cen = new Point.ImmutablePoint(cen.x0(), (int) (xLabel.y1() - s * textRenderer.lineHeight));
        m.identity();
        m.scaleAroundLocal(s, s, 1, cen.x0(), cen.y0(), 0);
        m.translate(cen.x0(), cen.y0(), 0);
        matrices.mulPoseMatrix(m);
        renderLabel(c, data.xLabel());
        matrices.popPose();
    }

    private void renderLabel(GuiGraphics c, String text)
    {
        GUIUtil.drawCenteredText(c, textRenderer,
                Component.literal(text).setStyle(GuideCols.style().withFont(unifont)), 0, 0, GuideCols.textCol(), false);
    }

    private void renderLines(GuiGraphics c, Rectangle bounds, boolean mouse, float mx, float my)
    {
        c.enableScissor(bounds.x0(), bounds.y0(), bounds.x1(), bounds.y1());
        var points = data.points();
        int maxIdx = points.size() - 1;
        if (maxIdx <= 0)
            return;

        for (int i = 0; i <= maxIdx - 1; i++)
        {
            GraphData.Point p0 = points.get(i);
            GraphData.Point p1 = points.get(i + 1);

            double nx0 = data.xRange().map(data.semiLogX, p0.x());
            double nx1 = data.xRange().map(data.semiLogX, p1.x());

            double ny0 = 1 - data.yRange().map(false, p0.y());
            double ny1 = 1 - data.yRange().map(false, p1.y());

            var gp0 = unLocalise(bounds, nx0, ny0);
            var gp1 = unLocalise(bounds, nx1, ny1);

            int col = GuideCols.textCol();
//            if (closeTo(gp0.firstFloat(), gp0.secondFloat(), gp1.firstFloat(), gp1.secondFloat(), mx, my))
//            {
//                col = 0xffff0000;
//            }

            GraphPane.drawLine(c, gp0.firstFloat(), gp0.secondFloat(), gp1.firstFloat(), gp1.secondFloat(), 0.5f, col);

        }
        c.flush();
        c.disableScissor();
    }

    private boolean closeTo(float x0, float y0, float x1, float y1, float mx, float my)
    {
        float d = 4;

        if (mx < x0 - d || mx > x1 + d || my < y0 - d || my > y1 + d)
            return false;

        float m = (y1 - y0) / (x1 - x0);
        float c = y0 - m * x0;

        float b = -1;

        var distanceToLine = Math.abs(m * mx + b * my + c) / Math.sqrt(m * m + b * b);

        return distanceToLine < 10;
    }

    private float getX(GraphData.Point point)
    {
        if (data.semiLogX)
            return NMMaths.log10(point.x());
        return point.x();
    }

    private static FloatFloatPair unLocalise(Rectangle rectangle, double tx, double ty)
    {
        return FloatFloatPair.of(
                (float) (rectangle.x0() + tx * rectangle.nw()),
                (float) (rectangle.y0() + ty * rectangle.nh())
        );
    }
}
