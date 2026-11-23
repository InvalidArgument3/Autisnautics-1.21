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

package com.neep.neepbus.block.entity;

import com.neep.neepbus.part.Indicator;
import org.jetbrains.annotations.Nullable;

public interface GaugeBlockEntity extends Indicator
{
    GaugeBlockEntity EMPTY = new GaugeBlockEntity()
    {
        @Override
        public @Nullable String getName()
        {
            return null;
        }

        @Override
        public boolean getUsePrefix()
        {
            return false;
        }

        @Override
        public String getUnit()
        {
            return "";
        }

        @Override
        public void setValues(float value, float minValue, float maxValue, String name, boolean usePrefix, String unit)
        {

        }

        @Override
        public float getValue()
        {
            return 0;
        }

        @Override
        public float getMinValue()
        {
            return 0;
        }

        @Override
        public float getMaxValue()
        {
            return 0;
        }
    };
}
