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

package com.neep.neepmeat.api.processing.random_ores;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;

import java.util.Locale;

enum Function
{
    ADD,
    MUL,
    ;

    static final Codec<Function> CODEC = new PrimitiveCodec<>()
    {
        @Override
        public <T> DataResult<Function> read(DynamicOps<T> ops, T input)
        {
            return ops.getStringValue(input).map(s -> Function.valueOf(s.toUpperCase(Locale.ROOT)));
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Function value)
        {
            return ops.createString(value.toString().toLowerCase(Locale.ROOT));
        }
    };
}
