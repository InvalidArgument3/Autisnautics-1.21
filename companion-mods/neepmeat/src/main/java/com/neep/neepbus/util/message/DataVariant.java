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

package com.neep.neepbus.util.message;

import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.meatlib.util.StringTokenView;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.vm.WorldTargetImpl;
import com.neep.neepmeat.plc.instruction.Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

public interface DataVariant
{
    DataVariant NULL = of(0);

    ParamCodec<DataVariant> PARAM_CODEC = ParamCodec.of(DataVariant.class, (o, b) -> b.writeUtf(o.stringValue()), b -> of(b.readUtf()));

    Pattern FLOAT_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?([eE]?\\d+)?");

    default int getAsInt()
    {
        return Math.round(getAsFloat());
    }

    float getAsFloat();
    String stringValue();
    @Nullable WorldTargetImpl wtValue();

    static float unquantizeCoord(String s)
    {
        if (FLOAT_PATTERN.asMatchPredicate().test(s))
        {
            try
            {
                return Float.parseFloat(s);
            }
            catch (NumberFormatException ignored) { }
        }

        return 0;
    }

    static DataVariant of(float f)
    {
        return new FloatDataVariant(f);
    }

    static DataVariant of(String s)
    {
        if (FLOAT_PATTERN.asMatchPredicate().test(s))
        {
            try
            {
                return new FloatDataVariant(Float.parseFloat(s));
            }
            catch (NumberFormatException ignored) { }
        }

        return new StringDataVariant(s);
    }

    default boolean isNumeric()
    {
        return FLOAT_PATTERN.asMatchPredicate().test(stringValue());
    }

    boolean boolValue();

    record FloatDataVariant(float f) implements DataVariant
    {
        @Override
        public float getAsFloat()
        {
            return f;
        }

        @Override
        public String stringValue()
        {
            return String.valueOf(f);
        }

        @Override
        public WorldTargetImpl wtValue()
        {
            return null;
        }

        @Override
        public boolean boolValue()
        {
            return f != 0;
        }

        @Override
        public @NotNull String toString()
        {
            return stringValue();
        }

        @Override
        public boolean isNumeric()
        {
            return true;
        }
    }

    record StringDataVariant(String s) implements DataVariant
    {
        @Override
        public float getAsFloat()
        {
            return DataVariant.unquantizeCoord(s);
        }

        @Override
        public String stringValue()
        {
            return s;
        }

        @Override
        public WorldTargetImpl wtValue()
        {
            // Absolute oof
            try
            {
                var thing = NeepAsmParser.parseWorldTarget1(new StringTokenView(s));
                return new WorldTargetImpl(thing.pos(), thing.face());
            }
            catch (NeepASM.ParseException e)
            {
                return null;
            }
        }

        @Override
        public boolean boolValue()
        {
            return !s.isEmpty();
        }

        @Override
        public @NotNull String toString()
        {
            return stringValue();
        }
    }
}
