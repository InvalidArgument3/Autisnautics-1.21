/*
 * Copyright (c) MeatWheeze 2026.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.plc.instruction;

import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.compiler.parser.ParsedInstruction;
import com.neep.neepmeat.neepasm.vm.VM;
import com.neep.neepmeat.plc.Instructions;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RisingEdgeInstruction implements Instruction
{
    private int prevValue;

    public RisingEdgeInstruction(CompoundTag nbt)
    {
        this.prevValue = nbt.getInt("prev_value");
    }

    public RisingEdgeInstruction()
    {
        this.prevValue = 0;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.putInt("prev_value", prevValue);
        return nbt;
    }

    @Override
    public void start(VM plc) throws NeepASM.RuntimeException
    {
        var dataStack = plc.dataStack();
        int newValue = dataStack.pop().intValue();
        if (prevValue <= 0 && newValue > 0)
            dataStack.push(-1);
        else
            dataStack.push(0);

        prevValue = newValue;
        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.RISING_EDGE;
    }

    public static ParsedInstruction parser(NeepAsmTokenView neepAsmTokenView, NeepAsmParser neepAsmParser, @Nullable String s)
    {
        return (labelLookup, program) -> new RisingEdgeInstruction();
    }
}
