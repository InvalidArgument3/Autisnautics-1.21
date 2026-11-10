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
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;

public class HashedBackground implements Renderable
{
    private final int padding;
    private final boolean inner;
    private int x;
    private int y;
    private int w;
    private int h;

    // The padding here is used in a completely different way to all the other borders I implemented.
    // This is because I'm an idiot.
    // Hopefully this is the correct way.
    public HashedBackground(int x, int y, int w, int h, int padding)
    {
        this(x, y, w, h, padding, false);
    }

    public HashedBackground(int x, int y, int w, int h, int padding, boolean inner)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.padding = padding;
        this.inner = inner;
    }

    public HashedBackground(Rectangle bounds, int padding, Supplier<Integer> col)
    {
        this(bounds.x0(), bounds.y0(), bounds.nw(), bounds.nh(), padding);
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public void setW(int w)
    {
        this.w = w;
    }

    public void setH(int h)
    {
        this.h = h;
    }

    public Rectangle withoutPadding()
    {
        return inner ? new Rectangle.ImmutableRect(x + padding, y + padding, w - 2 * padding, h - 2 * padding) : new Rectangle.ImmutableRect(x, y, w, h);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        if (inner)
            GUIUtil.drawHashedScreenBorder(context, x, y, w, h);
        else
            GUIUtil.drawHashedScreenBorder(context, x - padding, y - padding, w + 2 * padding, h + 2 * padding);
    }

    public int padding()
    {
        return padding;
    }
}
