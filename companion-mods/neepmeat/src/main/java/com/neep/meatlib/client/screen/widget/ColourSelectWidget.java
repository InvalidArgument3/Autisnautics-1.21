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

package com.neep.meatlib.client.screen.widget;

import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.auto.Panel;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.theme.PLCCols;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class ColourSelectWidget extends Panel
{
    private final IntConsumer update;
    private final int a = 0xff; // TODO
    private int r;
    private int g;
    private int b;

    private final List<Renderable> drawables = new ObjectArrayList<>();
    private final List<GuiEventListener> children = new ObjectArrayList<>();

    private boolean dirty;
    private Consumer<ColourSelectWidget> updatePreview = i -> {};

    public ColourSelectWidget(int initial, IntConsumer update)
    {
        this.r = initial >> 16 & 0xff;
        this.g = initial >> 8 & 0xff;
        this.b = initial & 0xff;
        this.update = update;
    }

    public ColourSelectWidget preview(Consumer<ColourSelectWidget> preview)
    {
        this.updatePreview = preview;
        return this;
    }

    private <T extends Renderable & GuiEventListener> T addChild(T child)
    {
        children.add(child);
        drawables.add(child);
        return child;
    }

    @Override
    public void init()
    {
        drawables.clear();
        children.clear();

        h = 30;

        int ey = y;
        int eh = 12;
        ColourBar child;
        child = addChild(new ColourBar(x + 1, ey, () -> r, this::setR, new int[]{0, 1, 1}));
        child.setDims(x, ey, w - 1, eh);
        ey += eh;
        child = addChild(new ColourBar(x + 1, ey, () -> g, this::setG, new int[]{1, 0, 1}));
        child.setDims(x, ey, w - 1, eh);
        ey += eh;
        child = addChild(new ColourBar(x + 1, ey, () -> b, this::setB, new int[]{1, 1, 0}));
        child.setDims(x, ey, w - 1, eh);
        ey += eh;

        this.h = ey - y;
    }

    public void setR(int r)
    {
        this.r = r;
        updatePreview.accept(this);
        dirty = true;
    }

    public void setG(int g)
    {
        this.g = g;
        updatePreview.accept(this);
        dirty = true;
    }

    public void setB(int b)
    {
        this.b = b;
        updatePreview.accept(this);
        dirty = true;
    }

    private void update()
    {
        update.accept(get());
        dirty = false;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        GUIUtil.renderBorderInner(context, x, y, w, h, PLCCols.TEXT.col, 0);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        update();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public static void fillGradient(PoseStack matrices, VertexConsumer vertexConsumer, float startX, float startY, float endX, float endY, float r1, float g1, float b1, float r2, float g2, float b2)
    {
        float z = 0;
        float a1 = 1;
        float a2 = 1;

        Matrix4f matrix4f = matrices.last().pose();
        vertexConsumer.addVertex(matrix4f, startX, startY, z).setColor(r1, g1, b1, a1);
        vertexConsumer.addVertex(matrix4f, startX, endY, z).setColor(r1, g1, b1, a1);
        vertexConsumer.addVertex(matrix4f, endX, endY, z).setColor(r2, g2, b2, a2);
        vertexConsumer.addVertex(matrix4f, endX, startY, z).setColor(r2, g2, b2, a2);
    }

    @Override
    protected List<Renderable> drawables()
    {
        return drawables;
    }

    @Override
    public List<GuiEventListener> children()
    {
        return children;
    }

    @Override
    public void close()
    {
        if (dirty)
            update();
        super.close();
    }

    public int get()
    {
        return  (a << 24) | (r << 16) | (g << 8) | b;
    }

    public void set(int col)
    {
//        this.a = (col >> 24) & 0xff;
        this.r = (col >> 16) & 0xff;
        this.g = (col >> 8) & 0xff;
        this.b = (col) & 0xff;
        updatePreview.accept(this);
        dirty = true;
    }

    public class ColourBar implements Rectangle.Mutable, ClickableWidget
    {
        private int x, y, w, h;

        private final IntSupplier value;
        private final IntConsumer set;
        private final int[] a;

        public ColourBar(int x, int y, IntSupplier value, IntConsumer set, int[] a)
        {
            this.x = x;
            this.y = y;
            this.value = value;
            this.set = set;
            this.a = a;
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            fillGradient(context.pose(),
                    context.bufferSource().getBuffer(RenderType.gui()),
                    x + 1, y, x + w, y + h,
                    a[0] * r / 255f,
                    a[1] * g / 255f,
                    a[2] * b / 255f,
                    a[0] > 0 ? r / 255f : 1,
                    a[1] > 0 ? g / 255f : 1,
                    a[2] > 0 ? b / 255f : 1);

            int cx = (int) (x + (value.getAsInt() / 255f) * w);
//            GUIUtil.drawVerticalLine1(context, cx, y, y + h, 0xffffffff);
            float s = 0.7f;
            PoseStack matrices = context.pose();
            matrices.pushPose();
            matrices.translate(cx, y + h / 2f - 2.5, 0);
            matrices.scale(s, s, 1);
            GUIUtil.drawCenteredText(context, Minecraft.getInstance().font, Component.literal("☐"), 0, 0, 0xffffffff, true);
            matrices.popPose();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button)
        {
            if (isWithin(mouseX, mouseY))
            {
                double f = (mouseX - x) / (float) this.w;
                set.accept((int) (f * 255));
            }

            return ClickableWidget.super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
        {
            return mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void setDims(int x, int y, int w, int h)
        {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        @Override
        public int x0()
        {
            return x;
        }

        @Override
        public int y0()
        {
            return y;
        }

        @Override
        public int nw()
        {
            return w;
        }

        @Override
        public int nh()
        {
            return h;
        }

        @Override
        public void setFocused(boolean focused)
        {

        }

        @Override
        public boolean isFocused()
        {
            return false;
        }
    }
}
