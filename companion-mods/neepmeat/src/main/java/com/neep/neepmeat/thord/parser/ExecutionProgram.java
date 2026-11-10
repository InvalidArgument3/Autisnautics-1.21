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

package com.neep.neepmeat.thord.parser;

import com.neep.neepmeat.api.plc.program.MutableProgram;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.program.Label;
import com.neep.neepmeat.plc.instruction.Instruction;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;

/// I no longer know what this is for
public class ExecutionProgram implements MutableProgram
{
    private final List<Instruction> instructions = new ObjectArrayList<>();

    public ExecutionProgram()
    {
    }

    @Override
    public void addBack(Instruction instruction) throws NeepASM.CompilationException
    {
        instructions.add(instruction);
    }

    @Override
    public void setDebugLine(int line) { }

    @Override
    public void addSymbol(Label symbol) { }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt) { return nbt; }

    @Override
    public void readNbt(CompoundTag nbt) { }

    @Override
    public Instruction get(int offset)
    {
        if (offset >= 0 && offset < instructions.size())
            return instructions.get(offset);

        return Instruction.end();
    }

    @Override
    public int size()
    {
        return instructions.size();
    }

    @Override
    public int getDebugLine(int counter)
    {
        return -1;
    }

    @Override
    public Map<String, Integer> exportedSymbols()
    {
        return Map.of();
    }
}
