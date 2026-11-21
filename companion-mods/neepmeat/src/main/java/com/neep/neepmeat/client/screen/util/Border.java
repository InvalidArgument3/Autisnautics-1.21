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

package com.neep.neepmeat.client.screen.util;

import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.theme.PLCCols;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;

public class Border implements Rectangle.Mutable, Renderable
{
    protected int x;
    protected int y;
    protected int w;
    protected int h;
    protected final int padding;
    protected final Supplier<Integer> col;

    public Border(int x, int y, int w, int h, int padding, Supplier<Integer> col)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.padding = padding;
        this.col = col;
    }

    public Border(Rectangle bounds, int padding, Supplier<Integer> col)
    {
        this( bounds.x0(), bounds.y0(), bounds.nw(), bounds.nh(), padding, col);
    }

    public Rectangle withoutPadding()
    {
        return new ImmutableRect(x + padding, y + padding, w - padding * 2, h - padding * 2);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        GUIUtil.renderBorder(context, x, y, w - 1, h - 1, col.get(), 0);
        GUIUtil.renderBorder(context, x, y, w - 1, h - 1, PLCCols.TRANSPARENT.col, -1);
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

    public int padding()
    {
        return padding;
    }

    @Override
    public void setDims(int x, int y, int w, int h)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }
}
