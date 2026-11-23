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

import com.google.common.collect.Lists;
import com.neep.neepmeat.api.plc.instruction.CallInstruction;
import com.neep.neepmeat.api.plc.program.MutableProgram;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.parser.ParsedInstruction;
import com.neep.neepmeat.neepasm.compiler.parser.PlcParsedInstruction;
import com.neep.neepmeat.neepasm.compiler.parser.ParsedMacro;
import com.neep.neepmeat.neepasm.program.Label;
import com.neep.neepmeat.plc.instruction.ReturnInstruction;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class ParsedFunction implements InstructionAcceptor
{
    private final String name;
    private final Function<String, ParsedMacro> macroFinder;

    private final List<ObjectIntPair<ParsedInstruction>> instructions = Lists.newArrayList();
    private final List<Label> labels = Lists.newArrayList();

    /**
     * @param name Function name
     * @param macroFinder A link to the main program's list of macros
     */
    public ParsedFunction(String name, Function<String, ParsedMacro> macroFinder)
    {
        this.name = name;
        this.macroFinder = macroFinder;
    }

    @Override
    public void instruction(ParsedInstruction instruction, int line)
    {
        instructions.add(ObjectIntPair.of(instruction, line));
    }

    @Override
    public void instruction(int index, ParsedInstruction preInstruction, int line) throws NeepASM.CompilationException
    {
        if (index < 0 || index > instructions.size())
            throw new NeepASM.CompilationException("instruction at offset " + index + " cannot be replaced");

        if (index == instructions.size())
            instruction(preInstruction, line);
        else
            instructions.set(index, ObjectIntPair.of(preInstruction, line));
    }

    @Override
    public void function(ParsedFunction function) throws NeepASM.CompilationException
    {
        throw new NeepASM.CompilationException("cannot define function here");
    }

    @Override
    public void label(Label label)
    {
        labels.add(label);
    }

    @Override
    public int size()
    {
        return instructions.size();
    }

    @Override
    public @Nullable ParsedMacro findMacro(String name)
    {
        return macroFinder.apply(name);
    }

    // No mangling anymore. Function labels can be accessed normally.
    public String mangledName()
    {
        return mangle(name);
    }

    public static String mangle(String name)
    {
        return name;
    }

    public Label mangleLabel(String label, int index)
    {
        return new Label(ParsedSource.mangleLabel(label, name), index);
    }

    public String name()
    {
        return name;
    }

    // Executed during compilation
    public void handle(ServerLevel world, ParsedSource parsedSource, MutableProgram program) throws NeepASM.CompilationException
    {
        Label label = parsedSource.findLabel(mangledName());
        if (label == null)
            throw new NeepASM.CompilationException(name + ": label '" + mangledName() + "' does not exist");

        program.addBack(new CallInstruction(label));
    }

    // Executed after parsing
    public void expand(InstructionAcceptor acceptor)
    {
        acceptor.label(new Label(mangledName(), acceptor.size()));
        for (Label label : labels)
        {
            acceptor.label(new Label(label.name(), acceptor.size() + label.index()));
        }

        int line = -1;
        for (var pair : instructions)
        {
            acceptor.instruction(pair.key(), pair.valueInt());
            line = pair.valueInt();
        }
        acceptor.instruction((PlcParsedInstruction.of((world, source, program) -> program.addBack(ReturnInstruction.INSTANCE))), line);
    }
}
