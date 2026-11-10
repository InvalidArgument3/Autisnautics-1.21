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

package com.neep.neepmeat.util;

public class NMVec2f extends net.minecraft.world.phys.Vec2
{
    public NMVec2f(float x, float y)
    {
        super(x, y);
    }

    @Override
    public String toString()
    {
        return "x: " + x + ", y: " + y;
    }

    public float getX()
    {
        return x;
    }

    public float getY()
    {
        return y;
    }

    public boolean isWithin(float x1, float y1, float difference)
    {
        return NMMaths.isWithin(x, x1, difference) && NMMaths.isWithin(y, y1, difference);
    }
}
