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

package com.neep.neepmeat.neepasm.compiler.variable;

public interface Variable<T>
{
    T value();

    Class<T> type();

    int compare(Variable<?> v2);

    default boolean notEmpty()
    {
        return !type().equals(Void.class);
    }

    Variable<Void> EMPTY = new Variable<>()
    {
        @Override
        public Void value()
        {
            return null;
        }

        @Override
        public Class<Void> type()
        {
            return Void.class;
        }

        @Override
        public int compare(Variable<?> v2)
        {
            return -2;
        }
    };
}
