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
import com.neep.neepmeat.neepasm.compiler.parser.ParsedInstruction;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.thord.compiler.CompilerVM;

/**
 * Every word is an immediate word in the sense that it *can* modify the state of the compiler when it is expanded.
 * Most built-in words resolve to a single ParsedInstruction.
 * Immediate words are allowed to add multiple ParsedInstructions.
 * A non-immediate user-defined word adds a ParsedInstructions that resolves to CALL.
 */
public interface Word
{
    void expand(InstructionAcceptor acceptor, CompilerVM stack, int line) throws NeepASM.NeepAsmException;

    /**
     * A simple implementation that corresponds to a single instruction.
     * @param parsedInstruction The thing that makes the instruction.
     */
    static Word of(ParsedInstruction parsedInstruction)
    {
        return (InstructionAcceptor acceptor, CompilerVM stack, int line) -> acceptor.instruction(parsedInstruction, line);
    }

    static Word of(Instruction instruction)
    {
        return (InstructionAcceptor acceptor, CompilerVM stack, int line) -> acceptor.instruction(instruction, line);
    }
}
