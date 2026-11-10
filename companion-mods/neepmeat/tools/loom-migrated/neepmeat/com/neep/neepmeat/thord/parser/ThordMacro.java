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

import com.google.common.collect.Lists;
import com.neep.meatlib.util.StringTokenView;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.thord.token.ThordTokenView;

import java.util.List;

public class ThordMacro
{
    private final String name;
    private final String string;

    private final List<String> parameters;
    private final int startLine;

    public ThordMacro(String name, List<String> parameters, String string, int startLine)
    {
        this.name = name;
        this.string = string;
        this.parameters = parameters;
        this.startLine = startLine;
    }

    public String expand(ThordTokenView view) throws NeepASM.ParseException
    {
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
//        if (!parameters.isEmpty())
//        {
//            if (!NeepAsmParser.isComment(view) && !view.lineEnded())
//                throw new NeepASM.ParseException("too many macro arguments");
//        }

        String processed = string;
        for (int i = 0; i < parameters.size(); ++i)
        {
            processed = processed.replace("%" + parameters.get(i), arguments.get(i));
        }

        return processed;
    }

    public static String parseArgumentString(StringTokenView view)
    {
        view.fastForward();
        StringBuilder builder = new StringBuilder();
        while (view.peek() != ',' && !NeepAsmParser.isComment(view))
        {
            if (view.eof() || view.lineEnded() || view.peek() == ';')
                break;

            builder.append(view.next());
        }

        if (view.peek() == ';')
            view.next();

        return builder.toString().strip();
    }

    public String name()
    {
        return name;
    }

    public int getStartLine()
    {
        return startLine;
    }
}
