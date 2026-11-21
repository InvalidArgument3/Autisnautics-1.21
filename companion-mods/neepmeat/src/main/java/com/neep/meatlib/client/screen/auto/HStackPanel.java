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

import com.neep.meatlib.client.screen.primitive.Point;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.List;

/// Currently works with fixed-width elements
public class HStackPanel extends Panel
{
    protected final int padding;
    protected final int elementPadding;

    protected final boolean fitElements;

    protected final List<Point.Mutable> children = new ObjectArrayList<>();
    private final List<Renderable> drawables = new ObjectArrayList<>();
    private final List<GuiEventListener> elements = new ObjectArrayList<>();

    protected int elementsWidth;
    protected int sideMargin = 0;

    public HStackPanel(int padding, int elementPadding, boolean fitElements)
    {
        this.padding = padding;
        this.elementPadding = elementPadding;
        this.fitElements = fitElements;
    }

    public HStackPanel(int padding, int elementPadding)
    {
        this(padding, elementPadding, false);
    }

    protected int fitElements()
    {
        int maxH = 0;
        for (var child : children)
        {
            if (fitElements || !(child instanceof Rectangle.Mutable))
            {
                maxH = Math.max(((Rectangle) child).nh(), maxH);
            }
        }

        return maxH + 2 * padding + sideMargin;
    }

    protected int xOffset()
    {
        return 0;
    }

    @Override
    public void init()
    {
        int xOffset = xOffset();
        elementsWidth = 0;
        if (fitElements)
            h = fitElements();

        for (var child : children)
        {
            Rectangle rectangle = (Rectangle) child;

            {
                // Stick it to the top of the panel.
                child.setPos(x + padding + xOffset, y + padding);
            }

            xOffset += rectangle.nw() + elementPadding;
            elementsWidth += rectangle.nw() + elementPadding;
        }
    }

    @Override
    public int nw()
    {
        if (fitElements)
            w = fitElements();

        return w;
    }

    @Override
    protected List<Renderable> drawables()
    {
        return drawables;
    }

    @Override
    public List<GuiEventListener> children()
    {
        return elements;
    }

    public <T extends Renderable & Point.Mutable & Rectangle & GuiEventListener> HStackPanel addChild(T widget)
    {
        children.add(widget);
        drawables.add(widget);
        elements.add(widget);
        return this;
    }

    public <T extends Renderable & Point.Mutable & Rectangle & GuiEventListener> T addChildIn(T widget)
    {
        children.add(widget);
        drawables.add(widget);
        elements.add(widget);
        return widget;
    }
}
