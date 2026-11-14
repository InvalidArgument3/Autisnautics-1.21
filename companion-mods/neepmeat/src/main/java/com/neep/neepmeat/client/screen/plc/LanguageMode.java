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

package com.neep.neepmeat.client.screen.plc;

import com.neep.meatlib.api.network.ParamCodec;
import com.neep.neepmeat.api.storage.WorldSupplier;
import com.neep.neepmeat.plc.compiler.NeepAsmCompilePipeline;
import com.neep.neepmeat.plc.compiler.PLCCompilePipeline;
import com.neep.neepmeat.plc.compiler.ThordCompilePipeline;

import java.util.function.Function;

public enum LanguageMode implements Function<WorldSupplier, PLCCompilePipeline<?>>
{
    NEEPASM
    {
        @Override
        public PLCCompilePipeline<?> apply(WorldSupplier supplier)
        {
            return new NeepAsmCompilePipeline(supplier);
        }
    },
    THORD
    {
        @Override
        public PLCCompilePipeline<?> apply(WorldSupplier supplier)
        {
            return new ThordCompilePipeline(supplier);
        }
    },
    ;

    public static final ParamCodec<LanguageMode> PARAM_CODEC = ParamCodec.of(LanguageMode.class, (o, b) -> b.writeVarInt(o.ordinal()), b -> LanguageMode.values()[b.readVarInt()]);
}
