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

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.neep.neepmeat.api.PowerUtil;

import java.util.EnumMap;

public enum StructureProperty implements Formatter
{
    FAULT_TOLERANCE(0, StructurePropertyFormatter.DEFAULT_FLOAT_FORMAT::format),
    SPEED(1, StructurePropertyFormatter.DEFAULT_FLOAT_FORMAT::format),
    SELF_REPAIR(0, StructurePropertyFormatter::formatRepair), // Format in reciprocal seconds
    MAX_POWER(0, v -> PowerUtil.POWER_FORMAT.format(v) + PowerUtil.POWER_UNIT.getString()),
    MASS(1, StructurePropertyFormatter.DEFAULT_FLOAT_FORMAT::format),
    ;

//    public static final Codec<StructureProperty> CODEC = RecordCodecBuilder.create(instance ->
//            instance.group(Codec.INT.fieldOf("ordinal").forGetter(StructureProperty::ordinal))
//                    .apply(instance, id -> StructureProperty.values()[id]));
    public static final Codec<StructureProperty> CODEC = Codec.STRING.comapFlatMap(id -> DataResult.success(valueOf(StructureProperty.class, id)), Enum::name).stable();

    public static final EnumMap<StructureProperty, PropertyValue> EMPTY = new EnumMap<>(StructureProperty.class);

    private final float def;
    private final StructurePropertyFormatter formatter;

    StructureProperty(float def, StructurePropertyFormatter formatter)
    {
        this.def = def;
        this.formatter = formatter;
    }

    public float defaultValue()
    {
        return def;
    }

    public String format(PropertyValue value)
    {
        return formatter.format(value.value());
    }
}
