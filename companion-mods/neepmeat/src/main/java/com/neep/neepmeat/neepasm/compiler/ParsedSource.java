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
import com.neep.neepmeat.neepasm.compiler.alias.ParsedAlias;
import com.neep.neepmeat.neepasm.compiler.parser.ParsedInstruction;
import com.neep.neepmeat.neepasm.compiler.parser.PlcParsedInstruction;
import com.neep.neepmeat.neepasm.compiler.parser.ParsedMacro;
import com.neep.neepmeat.neepasm.program.Label;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ParsedSource implements InstructionAcceptor, LabelLookup, ParsedInstructionList
{
    private final List<ObjectIntPair<ParsedInstruction>> instructions = Lists.newArrayList();
    private final List<Label> labels = Lists.newArrayList();
    private final List<ParsedFunction> functions = Lists.newArrayList();
    private final List<ParsedAlias> aliases = Lists.newArrayList();
    private final List<ParsedMacro> macros = Lists.newArrayList();

    @Override
    public void instruction(ParsedInstruction preInstruction, int line)
    {
        instructions.add(ObjectIntPair.of(preInstruction, line));
    }

    @Override
    public void instruction(int index, ParsedInstruction preInstruction, int line)
    {
        instructions.set(index, ObjectIntPair.of(preInstruction, line));
    }

    @Override
    public void label(Label label)
    {
        labels.add(label);
    }

    public void function(ParsedFunction function)
    {
        functions.add(function);
    }

    @Override
    public int size()
    {
        return instructions.size();
    }

    public void alias(ParsedAlias alias)
    {
        aliases.add(alias);
    }

    public void macro(ParsedMacro macro)
    {
        macros.add(macro);
    }

    @Override
    public Iterable<ObjectIntPair<ParsedInstruction>> instructions()
    {
        return instructions;
    }

    @Override
    public Iterable<Label> symbols()
    {
        return labels;
    }

    public Iterable<ParsedFunction> functions()
    {
        return functions;
    }

    @Override
    public @Nullable Label findLabel(String label)
    {
        return labels.stream().filter(l -> l.name().equals(label)).findFirst().orElse(null);
    }

    @Override
    @Nullable
    public Label findLabel(String label, int origin, Label.Seek seek)
    {
        return switch (seek)
        {
            case FORWARDS -> LabelLookup.findForwards(labels, label, origin);
            case BACKWARDS -> LabelLookup.findBackwards(labels, label, origin);
            case ABSOLUTE -> findLabel(label);
        };
    }

    @Nullable
    public ParsedFunction findFunction(String name)
    {
        return functions.stream().filter(m -> m.name().equals(name)).findFirst().orElse(null);
    }

    @Override
    @Nullable
    public ParsedMacro findMacro(String name)
    {
        return macros.stream().filter(m -> m.name().equals(name)).findFirst().orElse(null);
    }

    @Nullable
    public ParsedAlias findAlias(String name)
    {
        return aliases.stream().filter(a -> a.name().equals(name)).findFirst().orElse(null);
    }

    public static String mangleLabel(String label, @Nullable String scope)
    {
        // Ignored label
        if (label.equals("_"))
            return "_";

        if (scope != null)
            return scope + "#" + label;

        return label;
    }
}
