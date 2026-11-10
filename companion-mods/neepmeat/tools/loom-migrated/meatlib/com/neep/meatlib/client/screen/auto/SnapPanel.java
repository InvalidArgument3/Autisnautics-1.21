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
import java.util.List;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;

public class SnapPanel<T extends Point.Mutable & Rectangle & GuiEventListener> extends Panel
{
    private final T widget;
    private final Snap snap;

    public SnapPanel(T widget)
    {
        this(Snap.MIDDLE, widget);
    }

    public SnapPanel(Snap snap, T widget)
    {
        this.widget = widget;
        this.snap = snap;
    }

    @Override
    public void init()
    {
        // Centered coordinates
        int mx = x + (w - widget.nw()) / 2;
        int my = y + (h - widget.nh()) / 2;

        int ww = widget.nw();
        int wh = widget.nh();
        switch (snap)
        {
            case MIDDLE ->
                setDims(widget, mx, my, ww, wh);
            case BOTTOM ->
                setDims(widget, mx, y1() - wh, ww, wh);
            case TOP ->
                setDims(widget, mx, this.y0(), ww, wh);
            case LEFT ->
                setDims(widget, x0(), my, ww, wh);
            case RIGHT ->
                setDims(widget, x1() - ww, my, ww, wh);
            case BOTTOM_LEFT ->
                setDims(widget, x0(), y1() - wh, ww, wh);
            case BOTTOM_RIGHT ->
                setDims(widget, x1() - ww, y1() - wh, ww, wh);
            case TOP_LEFT ->
                setDims(widget, x0(), this.y0(), ww, wh);
            case TOP_RIGHT ->
                setDims(widget, x1() - ww, this.y0(), ww, wh);
        }
    }

    @Override
    protected List<Renderable> drawables()
    {
        if (widget instanceof Renderable drawable)
            return List.of(drawable);
        else
            return List.of();
    }

    @Override
    public List<GuiEventListener> children()
    {
        return List.of(widget);
    }

    @Override
    public int nw()
    {
        return Math.max(w, widget.nw());
    }

    @Override
    public int nh()
    {
        return Math.max(h, widget.nh());
    }

    public enum Snap
    {
        MIDDLE,
        TOP,
        RIGHT,
        BOTTOM,
        LEFT,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT,
    }
}
