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
import com.neep.neepmeat.neepasm.compiler.InstructionAcceptor;
import com.neep.neepmeat.neepasm.compiler.LabelLookup;
import com.neep.neepmeat.neepasm.compiler.ParsedFunction;
import com.neep.neepmeat.neepasm.compiler.parser.ParsedInstruction;
import com.neep.neepmeat.neepasm.program.Label;
import com.neep.neepmeat.neepasm.vm.VM;
import com.neep.neepmeat.thord.word.Word;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// A thing that records Words and ParsedInstructions and packages them for expansion later.
public class ParsedImmediateWord implements InstructionAcceptor, LabelLookup
{
    private final List<ObjectIntPair<Word>> words = new ObjectArrayList<>();
    private final List<Label> labels = new ObjectArrayList<>();
    private final MutableProgram program;
    private final VM machine;

    public ParsedImmediateWord(VM machine)
    {
        this.machine = machine;
        this.program = new ExecutionProgram();
    }

    public void word(Word word, int line)
    {
        words.add(ObjectIntPair.of(word, line));
    }

    @Override
    public void instruction(ParsedInstruction preInstruction, int line)
    {
//        word((acceptor, stack, head, line1) -> acceptor.instruction(preInstruction, line), line);
        word((acceptor, stack, line1) -> preInstruction.build(this, program).start(machine), line);
    }

    @Override
    public void instruction(int index, ParsedInstruction preInstruction, int line)
    {
//        word((acceptor, stack, head, line1) -> acceptor.instruction(index, preInstruction, line), line);
    }

    @Override
    public void function(ParsedFunction function) throws NeepASM.CompilationException
    {
//        word((acceptor, stack, head, line1) -> acceptor.function(function), -1);
        throw new NeepASM.CompilationException("cannot define word here");
    }

    @Override
    public void label(Label label)
    {
        // Use acceptor size as the given label's offset is meaningless.
//        word((acceptor, stack, head, line1) -> acceptor.label(new Label(label.name(), acceptor.size())), -1);
        labels.add(new Label(label.name(), words.size()));
    }

    @Override
    public int size()
    {
        return words.size();
    }

    public List<ObjectIntPair<Word>> getWords()
    {
        return words;
    }

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
}
