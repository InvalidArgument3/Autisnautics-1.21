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

import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.client.screen.widget.ClickableWidget;
import net.minecraft.client.gui.GuiGraphics;

public class EmptyElement implements ClickableWidget, Rectangle.Mutable
{
    private int x, y, w, h;

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {

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

    @Override
    public void setDims(int x, int y, int w, int h)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
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
    public int x0()
    {
        return x;
    }

    @Override
    public int y0()
    {
        return y;
    }
}
