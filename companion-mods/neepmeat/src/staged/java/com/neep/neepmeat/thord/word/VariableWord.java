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

package com.neep.neepmeat.thord.word;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.InstructionAcceptor;
import com.neep.neepmeat.neepasm.program.Label;
import com.neep.neepmeat.plc.instruction.PushInstruction;
import com.neep.neepmeat.thord.compiler.CompilerVM;

public class VariableWord implements Word
{
    private final String name;

    public VariableWord(String name)
    {
        this.name = name;
    }

    @Override
    public void expand(InstructionAcceptor acceptor, CompilerVM stack, int line) throws NeepASM.NeepAsmException
    {
        acceptor.instruction((labelLookup, program) ->
        {
            // Variable addresses are stored using labels in the intermediate representation.
            Label label = labelLookup.findLabel(name);
            if (label == null)
                throw new NeepASM.CompilationException("variable " + name + " not found");

            return new PushInstruction(label.index());

        }, line);
    }
}
