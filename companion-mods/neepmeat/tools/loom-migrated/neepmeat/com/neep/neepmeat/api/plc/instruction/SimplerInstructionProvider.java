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

package com.neep.neepmeat.api.plc.instruction;

import com.neep.neepmeat.neepasm.compiler.parser.InstructionParser;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.plc.instruction.PlcInstruction;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class SimplerInstructionProvider implements InstructionProvider
{
    private final SimpleInstructionProvider.NbtConstructor nbtConstructor;
    private final InstructionParser parser;
    private final String shortName;

    public SimplerInstructionProvider(SimpleInstructionProvider.NbtConstructor nbtConstructor, InstructionParser parser, String shortName)
    {
        this.nbtConstructor = nbtConstructor;
        this.parser = parser;
        this.shortName = shortName.toLowerCase(Locale.ROOT);
    }

    @Override
    public String getShortName()
    {
        return shortName.toUpperCase();
    }

    @Override
    public String getParseName()
    {
        return shortName;
    }

    @Override
    public Instruction createFromNbt(Supplier<Level> world, CompoundTag nbt)
    {
        return nbtConstructor.create(world, nbt);
    }

    @Override
    public InstructionParser getParser()
    {
        return parser;
    }
}
