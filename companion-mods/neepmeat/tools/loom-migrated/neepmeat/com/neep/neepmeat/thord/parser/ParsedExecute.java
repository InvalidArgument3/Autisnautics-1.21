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

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.InstructionAcceptor;
import com.neep.neepmeat.neepasm.compiler.ParsedFunction;
import com.neep.neepmeat.neepasm.compiler.parser.ParsedInstruction;
import com.neep.neepmeat.neepasm.program.Label;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;

import java.util.List;

public class ParsedExecute implements InstructionAcceptor
{
    private final List<ObjectIntPair<ParsedInstruction>> parsedInstructions = new ObjectArrayList<>();

    public void word(ParsedInstruction word, int line)
    {
        parsedInstructions.add(ObjectIntPair.of(word, line));
    }

    @Override
    public void instruction(ParsedInstruction preInstruction, int line)
    {
        word(preInstruction, line);
    }

    @Override
    public void instruction(int index, ParsedInstruction preInstruction, int line)
    {
        parsedInstructions.set(index, ObjectIntPair.of(preInstruction, line));
    }

    @Override
    public void function(ParsedFunction function) throws NeepASM.CompilationException
    {
//        word((acceptor, stack, head, line1) -> acceptor.function(function), -1);
    }

    @Override
    public void label(Label label)
    {
//        word((acceptor, stack, head, line1) -> acceptor.label(label), -1);
    }

    @Override
    public int size()
    {
        return 0;
    }

    public List<ObjectIntPair<ParsedInstruction>> parsedInstructions()
    {
        return parsedInstructions;
    }
}
