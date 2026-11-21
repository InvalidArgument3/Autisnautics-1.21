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

package com.neep.neepmeat.transport.fluid_network.node;

import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.util.Mth;

public enum AcceptorModes
{

    NONE(0, 0),
    PULL(1, -1),
    PUSH(2, 1),
    INSERT_ONLY(3, 0),
    EXTRACT_ONLY(4, 0),
    INSERT_EXTRACT(5, 0), // This is not used because I am stupid
    SENSOR(6, 0); // This is also not used because I am stupid

    private final float flow;
    private final int id;
    private static final AcceptorModes[] VALUES = Arrays.stream(values())
            .sorted(Comparator.comparingInt(mode -> mode.id))
            .toArray(AcceptorModes[]::new);

    public float getFlow()
    {
        return flow;
    }

    public int getId()
    {
        return id;
    }

    public boolean canInsert()
    {
        return this == INSERT_ONLY || this == INSERT_EXTRACT || this == PULL;
    }

    public boolean canFlow()
    {
        return this != NONE;
    }

    public boolean canExtract()
    {
        return this == EXTRACT_ONLY || this == INSERT_EXTRACT || this == PUSH;
    }

    public static AcceptorModes byId(int id)
    {
        return VALUES[Mth.abs(id % VALUES.length)];
    }

    public static AcceptorModes byFlow(float flow)
    {
        return flow < 0 ? PUSH : flow > 0 ? PULL : INSERT_EXTRACT;
    }

    AcceptorModes(int id, float flow)
    {
        this.flow = flow;
        this.id = id;
    }

    public boolean isDriving()
    {
        return this == PUSH || this == PULL;
    }
}
