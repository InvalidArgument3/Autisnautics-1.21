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

public interface Rectangle extends Point
{
    int x0();

    int y0();

    int nw();

    int nh();

    default Rectangle offset(int dx, int dy)
    {
        return new ImmutableRect(x0() + dx, y0() + dy, nw(), nh());
    }

    default boolean isWithin(double mx, double my)
    {
        return mx >= x0() && mx <= x0() + nw()
                && my >= y0() && my <= y0() + nh();
    }

    default int x1()
    {
        return x0() + nw();
    }

    default int y1()
    {
        return y0() + nh();
    }

    default Rectangle withTopLeft(int x, int y)
    {
        return new ImmutableRect(x, y, nw(), nh());
    }

    default Point getCentre() { return new ImmutablePoint(x0() + nw() / 2, y0() + nh() / 2); }

    interface Mutable extends Rectangle, Point.Mutable
    {
        default void setSize(int w, int h)
        {
            setDims(x0(), y0(), w, h);
        }

        default void setPos(int x, int y)
        {
            setDims(x, y, nw(), nh());
        }

        /**
         * It is preferred to call this over setSize and setPos together.
         */
        void setDims(int x, int y, int w, int h);

        interface Delegate extends Rectangle.Delegate, Rectangle.Mutable
        {
            @Override
            default void setDims(int x, int y, int w, int h) { setDims(new ImmutableRect(x, y, w, h)); }

            void setDims(Rectangle newDims);
        }
    }

    class ImmutableRect implements Rectangle
    {
        public final int x, y, w, h;

        public ImmutableRect(Rectangle rectangle)
        {
            this.x = rectangle.x0();
            this.y = rectangle.y0();
            this.w = rectangle.nw();
            this.h = rectangle.nh();
        }

        public ImmutableRect(int x, int y, int w, int h)
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
    }

    interface Delegate extends Rectangle
    {
        Rectangle getDelegate();

        default int x0() { return getDelegate().x0(); }

        default int y0() { return getDelegate().y0(); }

        default int nw() { return getDelegate().nw(); }

        default int nh() { return getDelegate().nh(); }
    }
}
