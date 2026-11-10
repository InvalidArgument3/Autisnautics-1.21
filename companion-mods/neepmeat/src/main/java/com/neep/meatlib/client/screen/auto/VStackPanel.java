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

public class VStackPanel extends Panel
{
    protected final int padding;
    protected final int elementPadding;
    protected final boolean centre;

    protected final boolean fitElements;

    protected final List<Point.Mutable> children = new ObjectArrayList<>();
    private final List<Renderable> drawables = new ObjectArrayList<>();
    private final List<GuiEventListener> elements = new ObjectArrayList<>();

    protected int elementsHeight;
    protected int sideMargin = 0;

    public VStackPanel(int padding, int elementPadding, boolean centre, boolean fitElements)
    {
        this.padding = padding;
        this.elementPadding = elementPadding;
        this.centre = centre;
        this.fitElements = fitElements;
    }

    public VStackPanel(int padding, int elementPadding, boolean centre)
    {
        this(padding, elementPadding, centre, false);
    }

    protected int fitElementsWidth()
    {
        int maxW = 0;
        for (var child : children)
        {
            if (fitElements || !(child instanceof Rectangle.Mutable))
            {
                maxW = Math.max(((Rectangle) child).nw(), maxW);
            }
        }

        return maxW + 2 * padding + sideMargin;
    }

    protected int fitElementsHeight()
    {
        int height = 0;
        for (var child : children)
        {
            height += ((Rectangle) child).nh();
            height += elementPadding;
        }

        return height + 2 * padding + sideMargin;
    }

    protected int yOffset()
    {
        return 0;
    }

    @Override
    public void init()
    {
        int yOffset = yOffset();
        elementsHeight = 0;
        if (fitElements)
            w = fitElementsWidth();

        for (var child : children)
        {
            Rectangle rectangle = (Rectangle) child;

            if (child instanceof Rectangle.Mutable mutable)
            {
                // Stretch the widget all the way across, ignoring centring.
                mutable.setDims(x + padding, y + padding + yOffset, w - 2 * padding - sideMargin, mutable.nh());
            }
            else
            {
                // Centre the widget, or stick it to the left of the panel.
                if (centre)
                    child.setPos(x + padding + ((w - 2 * padding) - rectangle.nw()) / 2, y + yOffset);
                else
                    child.setPos(x + padding, y + padding + yOffset);

            }

            if (child instanceof ScreenInitialisable initialisable)
                initialisable.init();

            yOffset += rectangle.nh() + elementPadding;
            elementsHeight += rectangle.nh() + elementPadding;
        }
    }

    @Override
    public int nw()
    {
        if (fitElements)
            w = fitElementsWidth();

        return w;
    }

    @Override
    public int nh()
    {
        if (h == 0)
            h = fitElementsHeight();

        return h;
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

    public <T extends Renderable & Point.Mutable & Rectangle & GuiEventListener> VStackPanel addChild(T widget)
    {
        children.add(widget);
        drawables.add(widget);
        elements.add(widget);
        return this;
    }

    public <T extends Renderable & Point.Mutable & Rectangle & GuiEventListener> T addChildIn(T widget)
    {
        addChild(widget);
        return widget;
    }

    public void clearWidgets()
    {
        children.clear();
        drawables.clear();
        elements.clear();
    }
}
