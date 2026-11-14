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

package com.neep.neepmeat.api.plc.program;

import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.neepasm.program.Program;
import com.neep.neepmeat.plc.instruction.Instruction;
import net.minecraft.nbt.CompoundTag;

import java.util.Map;

public interface PLCProgram extends Program, NbtSerialisable
{
    Empty EMPTY = new Empty();

    Map<String, Integer> exportedSymbols();

    default String getName() { return ""; }

    class Empty implements PLCProgram
    {
        @Override
        public CompoundTag writeNbt(CompoundTag nbt) { return nbt; }

        @Override
        public void readNbt(CompoundTag nbt) { }

        @Override
        public Instruction get(int offset) { return Instruction.EMPTY; }

        @Override
        public int size() { return 0; }

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
}
