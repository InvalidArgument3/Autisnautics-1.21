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

package com.neep.neepmeat.neepasm.compiler.parser;

import com.google.common.collect.Lists;
import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.meatlib.util.StringTokenView;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.*;
import com.neep.neepmeat.neepasm.program.Label;

import java.util.List;
import java.util.Set;

// Stores the raw text of the macro allowing blind substitution of text arguments.
public class ParsedMacro
{
    private final String name;
    private final List<String> parameters;
    private final String macroText;
    private final int startLine;

    private int numExpansions;

    public ParsedMacro(String name, List<String> parameters, String macroText, int startLine)
    {
        this.name = name;
        this.parameters = parameters;
        this.macroText = macroText;
        this.startLine = startLine;
    }

    /**
     * @param above For checking for recursive expansions (direct or indirect)
     */
    public void expand(StringTokenView view, InstructionAcceptor parsedSource, NeepAsmParser parser, Set<ParsedMacro> above) throws NeepASM.ParseException
    {
        if (above.contains(this))
            throw new NeepASM.ParseException("recursive macro expansion: " + name);

        above.add(this);

        // Give each expansion a unique name.
        String localName = name + "#" + numExpansions;
        numExpansions++;

        // Skip over macro name
        view.nextIdentifier();

        List<String> arguments = Lists.newArrayList();
        for (int i = 0; i < parameters.size(); ++i)
        {
            view.fastForward();

            String arg = parseArgumentString(view);
            if (arg.isEmpty())
                throw new NeepASM.ParseException("not enough comma-separated macro arguments.");

            if (!view.lineEnded())
                view.next();
            arguments.add(arg);
        }

        view.fastForward();
        if (!NeepAsmParser.isComment(view) && !view.lineEnded())
            throw new NeepASM.ParseException("too many macro arguments");

        // Substitute
        String processed = macroText;
        for (int i = 0; i < parameters.size(); ++i)
        {
            processed = processed.replace("%" + parameters.get(i), arguments.get(i));
        }

        NeepAsmTokenView macroView = new NeepAsmTokenView(processed, startLine);
        while (!macroView.eof())
        {
            loadLine(macroView, parsedSource, parser, localName, above);
            macroView.nextLine();
        }
    }

    private void loadLine(NeepAsmTokenView view, InstructionAcceptor parsedSource, NeepAsmParser parser, String localName, Set<ParsedMacro> above) throws NeepASM.ParseException
    {
        String token;
        try (var entry = view.markPosition())
        {
            token = view.nextIdentifier();
            char follow = view.nextThing();
            if (follow == ':')
            {
                // Use the mangled label name here to prevent duplicate labels after repeated expansions.
                parsedSource.label(new Label(ParsedSource.mangleLabel(token, localName), parsedSource.size()));
                parser.assureLineEnd(view);

                entry.commit();
                return;
            }
        }

//        parser.parseInstructionOrMacro(parsedSource, view, token, localName);

        ParsedMacro macro = parsedSource.findMacro(token);
        if (macro != null)
        {
            macro.expand(view, parsedSource, parser, above);
        }
        else
        {
            ParsedInstruction instruction = parser.parseInstruction(view, localName);
            if (instruction != null)
                parsedSource.instruction(instruction, view.line());
        }
    }

    public static String parseArgumentString(StringTokenView view)
    {
        view.fastForward();
        StringBuilder builder = new StringBuilder();
        while (view.last() != ',' && !view.lineEnded() && !NeepAsmParser.isComment(view))
        {
            if (view.eof() || view.lineEnded())
                break;

            builder.append(view.next());
        }

        return net.minecraft.ChatFormatting.stripFormatting(builder.toString());
    }

    public String name()
    {
        return name;
    }
}
