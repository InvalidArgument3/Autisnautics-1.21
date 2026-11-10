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

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import org.joml.Vector3f;

public enum AxialDirection implements StringRepresentable
{
    POSITIVE(1, "positive"),
    NEGATIVE(-1, "negative");

    private final String id;
    private final int sign;

    AxialDirection(int sign, String id)
    {
        this.sign = sign;
        this.id = id;
    }

    @Override
    public String getSerializedName()
    {
        return id;
    }

    public int sign()
    {
        return sign;
    }

    public static AxialDirection from(Direction direction)
    {
        return direction.getAxis().choose(direction.getStepX(), direction.getStepY(), direction.getStepZ()) > 0 ?
                POSITIVE : NEGATIVE;
    }

    public Vector3f with(Direction.Axis direction)
    {
        Vector3f vec = switch (direction)
        {
            case X -> Direction.EAST.step();
            case Y -> Direction.UP.step();
            case Z -> Direction.SOUTH.step();
        };

        vec.mul(sign, sign, sign);
        return vec;
    }
}
