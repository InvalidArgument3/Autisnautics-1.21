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

package com.neep.neepmeat.neepasm.compiler;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.parser.ParsedInstruction;
import com.neep.neepmeat.neepasm.compiler.parser.ParsedMacro;
import com.neep.neepmeat.neepasm.program.Label;
import com.neep.neepmeat.plc.instruction.Instruction;
import org.jetbrains.annotations.Nullable;

public interface InstructionAcceptor
{
    default void instruction(Instruction instruction, int line)
    {
        instruction((labelLookup, program) -> instruction, line);
    }

    void instruction(ParsedInstruction preInstruction, int line);

    void instruction(int index, ParsedInstruction preInstruction, int line) throws NeepASM.CompilationException;
    void function(ParsedFunction function) throws NeepASM.CompilationException;
    void label(Label label);
    int size();
    @Nullable
    default ParsedMacro findMacro(String name) { return null; }

    InstructionAcceptor EMPTY = new InstructionAcceptor()
    {
        @Override
        public void instruction(ParsedInstruction preInstruction, int line)
        {

        }

        @Override
        public void instruction(int index, ParsedInstruction preInstruction, int line) throws NeepASM.CompilationException
        {
            throw new NeepASM.CompilationException("no acceptor");
        }

        @Override
        public void function(ParsedFunction function) throws NeepASM.CompilationException
        {
            throw new NeepASM.CompilationException("no acceptor");
        }

        @Override
        public void label(Label label)
        {

        }

        @Override
        public int size()
        {
            return 0;
        }
    };
}
