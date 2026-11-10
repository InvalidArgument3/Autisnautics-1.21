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
import com.neep.neepmeat.neepasm.compiler.LabelLookup;
import com.neep.neepmeat.neepasm.compiler.ParsedFunction;
import com.neep.neepmeat.neepasm.compiler.ParsedInstructionList;
import com.neep.neepmeat.neepasm.compiler.parser.ParsedInstruction;
import com.neep.neepmeat.neepasm.compiler.parser.ParsedMacro;
import com.neep.neepmeat.neepasm.program.Label;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ThordParsedSource implements LabelLookup, InstructionAcceptor, ParsedInstructionList
{
    private final List<ObjectIntPair<ParsedInstruction>> instructions = new ObjectArrayList<>();
    private final List<Label> labels = new ObjectArrayList<>();
    private final List<ParsedFunction> functions = new ObjectArrayList<>();

    @Override
    public @Nullable Label findLabel(String label)
    {
        return labels.stream().filter(l -> l.name().equals(label)).findFirst().orElse(null);
    }

    @Override
    public @Nullable Label findLabel(String label, int origin, Label.Seek seek)
    {
        return switch (seek)
        {
            case FORWARDS -> LabelLookup.findForwards(labels, label, origin);
            case BACKWARDS -> LabelLookup.findBackwards(labels, label, origin);
            case ABSOLUTE -> findLabel(label);
        };
    }

    @Override
    public void instruction(ParsedInstruction preInstruction, int line)
    {
        instructions.add(ObjectIntPair.of(preInstruction, line));
    }

    @Override
    public void instruction(int index, ParsedInstruction preInstruction, int line) throws NeepASM.CompilationException
    {
        if (index < 0 || index > instructions.size())
            throw new NeepASM.CompilationException("instruction at offset " + index + " cannot be replaced");

        if (index == instructions.size())
        {
            instruction(preInstruction, line);
        }
        else
        {
            int prevLine = instructions.get(index).valueInt();
            instructions.set(index, ObjectIntPair.of(preInstruction, prevLine));
        }
    }

    @Override
    public Iterable<ObjectIntPair<ParsedInstruction>> instructions()
    {
        return instructions;
    }

    @Override
    public Iterable<Label> symbols()
    {
        return labels; // TODO: difference between exported symbols and private ones
    }

    @Override
    public void label(Label label)
    {
        labels.add(label);
    }

    @Override
    public void function(ParsedFunction function)
    {
        functions.add(function);
    }

    public List<ParsedFunction> functions()
    {
        return functions;
    }

    @Override
    public int size()
    {
        return instructions.size();
    }

    @Override
    public @Nullable ParsedMacro findMacro(String name)
    {
        return null;
    }
}
