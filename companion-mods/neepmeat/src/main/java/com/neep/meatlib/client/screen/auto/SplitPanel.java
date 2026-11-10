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

package com.neep.meatlib.client.screen.auto;

import com.neep.meatlib.client.screen.widget.ClickableWidget;
import com.neep.meatlib.client.screen.primitive.Point;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// Use 0 padding if any sub elements have solid dimensions but inherit from Rectangle.Mutable
/// Placement assumes flexibility (and is incorrect for non-mutable rectangles)
/// It all needs changing.
public class SplitPanel extends Panel
{
    private final Split split;
    private final int padding;
    @Nullable protected ClickableWidget left;
    @Nullable protected ClickableWidget right;
    private Divide divide = null;
    private float divideFraction = 0.5f;
    protected boolean centre;
    @Nullable protected Fit fit;
    private int separation = 0;

    public SplitPanel(Split split, int padding, @Nullable ClickableWidget left, @Nullable ClickableWidget right)
    {
        this.split = split;
        this.padding = padding;
        this.left = left;
        this.right = right;
    }

    public SplitPanel(Split split, int padding)
    {
        this(split, padding, null, null);
    }

    @Override
    public void init()
    {
        if (divide == null)
            divide = new Divide.Rel(Anchor.START, divideFraction);

        if (fit != null)
        {
            if (fit == Fit.FIRST)
                divide = new Divide.Abs(Anchor.START, split.majorDimension(left));
            else if (fit == Fit.SECOND)
                divide = new Divide.Abs(Anchor.START, split.majorDimension(this) - split.majorDimension(right));
        }

        // Separation is only removed from the second element.
        // Please change in the future

        int div = this.divide.amount(split.majorDimension(this)); // TODO: AAAAAAA
        int leftPanelSize = div - padding - padding / 2;

        if (split == Split.HORIZONTAL)
        {
            int rightPanelSize = (w - div) - padding - padding / 2 - separation;
            if (left != null)
            {
                setDims(left, x + padding, y + padding, leftPanelSize, h - 2 * padding);

                // Secondary pass is necessary as some elements may change size.
                if (centre)
                    left.setPos(left.x0(), y + padding + (nh() - 2 * padding - left.nh()) / 2);
            }

            if (right != null)
            {
                setDims(right, x + padding + div + padding / 2 + separation, y + padding, rightPanelSize, h - 2 * padding);

                if (centre)
                    right.setPos(right.x0(), y + padding + (nh() - 2 * padding - right.nh()) / 2);
            }
        }
        else
        {
            int rightPanelSize = (h - div) - padding - padding / 2 - separation;
            if (left != null)
            {
                setDims(left, x + padding, y + padding, w - 2 * padding, leftPanelSize);

                // Secondary pass is necessary as some elements may change size.
                if (centre)
                    left.setPos(x + padding + (nw() - 2 * padding - left.nw()) / 2, left.y0());
            }

            if (right != null)
            {
                setDims(right, x + padding, y + padding + div + padding / 2 + separation, w - 2 * padding, rightPanelSize);

                if (centre)
                    right.setPos(x + padding + (nw() - 2 * padding - right.nw()) / 2, right.y0());
            }
        }
    }

    protected static void setDims(GuiEventListener element, int x, int y, int w, int h, boolean centre)
    {
        if (element instanceof Rectangle.Mutable mutable)
            mutable.setDims(x, y, w, h);

        else if (element instanceof Point.Mutable mutable)
            mutable.setPos(x, y);

    }

    @Override
    protected List<Renderable> drawables()
    {
        List<Renderable> drawables = new ObjectArrayList<>();
        if (left != null)
            drawables.add(left);
        if (right != null)
            drawables.add(right);

        return drawables;
    }

    @Override
    public List<GuiEventListener> children()
    {
        List<GuiEventListener> drawables = new ObjectArrayList<>();
        if (left != null)
            drawables.add(left);
        if (right != null)
            drawables.add(right);

        return drawables;
    }

    @Override
    public int nh()
    {
        if (split == Split.HORIZONTAL && h == 0)
            return Math.max(left != null ? left.nh() : 0, right != null ? right.nh() : 0);

        return super.nh();
    }

    @Override
    public int nw()
    {
        if (split == Split.VERTICAL && w == 0)
            return Math.max(left != null ? left.nw() : 0, right != null ? right.nw() : 0);

        return super.nw();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
//        debugOutline(context);
    }

    public SplitPanel setDivide(Divide divide)
    {
        this.divide = divide;
        return this;
    }

    public SplitPanel setFit(Fit fit)
    {
        this.fit = fit;
        return this;
    }

    public SplitPanel setA(@Nullable ClickableWidget left)
    {
        this.left = left;
        return this;
    }

    public SplitPanel setB(@Nullable ClickableWidget right)
    {
        this.right = right;
        return this;
    }

    public SplitPanel centre(boolean centre)
    {
        this.centre = centre;
        return this;
    }

    public SplitPanel separation()
    {
        this.separation = 1;
        return this;
    }

    public enum Split
    {
        HORIZONTAL,
        VERTICAL;

        public int majorDimension(Rectangle rectangle)
        {
            return switch (this)
            {
                case VERTICAL -> rectangle.nh();
                case HORIZONTAL -> rectangle.nw();
            };
        }

        public int minorDimension(Rectangle rectangle)
        {
            return switch (this)
            {
                case VERTICAL -> rectangle.nw();
                case HORIZONTAL -> rectangle.nh();
            };
        }
    }

    public enum Fit
    {
        FIRST,
        SECOND
    }
}
