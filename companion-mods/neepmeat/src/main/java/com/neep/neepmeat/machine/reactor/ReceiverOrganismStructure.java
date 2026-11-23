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

package com.neep.neepmeat.machine.reactor;

import com.neep.neepmeat.api.live_machine.Formatter;
import com.neep.neepmeat.api.live_machine.PropertyValue;
import com.neep.neepmeat.api.live_machine.StructurePropertyFormatter;

import java.util.EnumMap;

public interface ReceiverOrganismStructure
{
    EnumMap<Property, PropertyValue> getProperties();

    enum Property implements Formatter
    {
        ORGANISATION(0.1f, StructurePropertyFormatter.DEFAULT_FLOAT_FORMAT::format);

        private final float defaultValue;
        private final StructurePropertyFormatter formatter;

        @Override
        public String format(PropertyValue value)
        {
            return formatter.format(value.value());
        }

        public float defaultValue()
        {
            return defaultValue;
        }

        Property(float defaultValue, StructurePropertyFormatter formatter)
        {
            this.defaultValue = defaultValue;
            this.formatter = formatter;
        }
    }
}
