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

public interface Divide
{
    int amount(int dimension);

    record Abs(Anchor anchor, int amount) implements Divide
    {
        public Abs(int amount)
        {
            this(Anchor.START, amount);
        }

        @Override
        public int amount(int dimension)
        {
            return anchor.apply(dimension, amount);
        }
    }

    record Rel(Anchor anchor, float rel) implements Divide
    {
        public Rel(float rel)
        {
            this(Anchor.START, rel);
        }

        public int amount(int dimension)
        {
            return anchor.apply(dimension, Math.round(rel * dimension));
        }
    }
}
