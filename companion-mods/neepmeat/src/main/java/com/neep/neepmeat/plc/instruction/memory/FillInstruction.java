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

package com.neep.neepmeat.plc.instruction.memory;

import com.neep.neepmeat.api.plc.PLC;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import com.neep.neepmeat.plc.instruction.PlcInstruction;
import org.jetbrains.annotations.NotNull;

public class FillInstruction implements PlcInstruction
{
    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        var stack = plc.dataStack();
        var value = stack.pop();
        int size = stack.pop().pointerValue();
        int addr = stack.pop().pointerValue();
        var memory = plc.getMemory();
        for (int i = 0; i < size; ++i)
        {
            memory.store(addr + i, value);
        }
        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.FILL;
    }
}
