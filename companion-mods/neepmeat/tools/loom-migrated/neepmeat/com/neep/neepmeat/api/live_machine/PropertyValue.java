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

package com.neep.neepmeat.api.live_machine;

import com.google.common.util.concurrent.AtomicDouble;

public record PropertyValue(Function function, float value)
{
    public PropertyValue(float value)
    {
        this(Function.AVERAGE, value);
    }

    public void apply(AtomicDouble atomicDouble, int count)
    {
        switch (function)
        {
            case ADD -> atomicDouble.addAndGet(value);
            case AVERAGE -> atomicDouble.addAndGet(value / count);
        }
    }

    public float apply(float f, int count)
    {
        return switch (function)
        {
            case ADD -> f + value;
            case AVERAGE -> f + (value / count);
        };
    }

    public enum Function
    {
        ADD("added"),
        AVERAGE("averaged");

        public final String name;

        Function(String name)
        {
            this.name = name;
        }

        public boolean average()
        {
            return this == AVERAGE;
        }
    }
}
