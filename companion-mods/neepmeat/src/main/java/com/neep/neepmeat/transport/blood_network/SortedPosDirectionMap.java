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

package com.neep.neepmeat.transport.blood_network;

import java.util.Comparator;
import java.util.stream.Stream;

public class SortedPosDirectionMap<T> extends PosDirectionMap<T>
{
    private final Comparator<T> comparator;

    public SortedPosDirectionMap(Class<T> clazz, Comparator<T> comparator)
    {
        super(clazz);
        this.comparator = comparator;
    }

    @Override
    public Stream<T> flatStream()
    {
        return super.flatStream().sorted(comparator);
    }
}
