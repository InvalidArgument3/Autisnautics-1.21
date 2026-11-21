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

package com.neep.neepmeat.transport.fluid_network;

import net.minecraft.util.StringRepresentable;

public enum SimplePipeConnectionType implements StringRepresentable
{
    SIDE("true"),
    NONE("false");

    private final String name;

    SimplePipeConnectionType(String name)
    {
        this.name = name;
    }

    @Override
    public String getSerializedName()
    {
        return this.name;
    }

    public boolean isConnected()
    {
        return this == SIDE;
    }

    public boolean canBeChanged()
    {
        return this == SIDE || this == NONE;
    }
}
