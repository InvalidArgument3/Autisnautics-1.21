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

package com.neep.meatlib.client.screen.primitive;

public class MutableRectangle implements Rectangle.Mutable
{
    private int x;
    private int y;

    public MutableRectangle set(int x, int y, int w, int h)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        return this;
    }

    public MutableRectangle setX(int x)
    {
        this.x = x;
        return this;
    }

    public MutableRectangle setY(int y)
    {
        this.y = y;
        return this;
    }

    public MutableRectangle setW(int w)
    {
        this.w = w;
        return this;
    }

    public MutableRectangle setH(int h)
    {
        this.h = h;
        return this;
    }

    private int w;
    private int h;

    public MutableRectangle(Rectangle rectangle)
    {
        this.x = rectangle.x0();
        this.y = rectangle.y0();
        this.w = rectangle.nw();
        this.h = rectangle.nh();
    }

    public MutableRectangle(int x, int y, int w, int h)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public MutableRectangle()
    {
        this(0, 0, 0, 0);
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
    public void setDims(int x, int y, int w, int h)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public MutableRectangle shaveLeft(int amount)
    {
        var ret = new MutableRectangle(x, y, amount, h);
        x += amount;
        w -= amount;
        return ret;
    }

    public MutableRectangle shaveTop(int amount)
    {
        var ret = new MutableRectangle(x, y, w, amount);
        y += amount;
        h -= amount;
        return ret;
    }

    public MutableRectangle shaveBottom(int amount)
    {
        h -= amount;
        return new MutableRectangle(x, y + h, w, amount);
    }

    public MutableRectangle grow(int amount)
    {
        w += amount * 2;
        h += amount * 2;
        x -= amount;
        y -= amount;
        return this;
    }
}
