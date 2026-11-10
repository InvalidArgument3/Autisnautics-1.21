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

package com.neep.meatlib.larkdown.parser;

import com.neep.meatlib.larkdown.LarkdownParseException;
import com.neep.meatlib.larkdown.LarkdownParser;
import com.neep.meatlib.larkdown.command.CommandArguments;
import com.neep.meatlib.larkdown.command.CommandOptions;
import com.neep.meatlib.larkdown.command.LarkdownCommand;
import com.neep.meatlib.larkdown.command.LarkdownCommands;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

public class LineCommandParser implements LarkdownParser.ContentParser
{
    private final LarkdownCommands commands;

    public LineCommandParser(LarkdownCommands commands)
    {
        this.commands = commands;
    }

    @Override
    public boolean matches(String line)
    {
        return line.startsWith("\\");
    }

    @Override
    public void parse(BufferedReader reader, LarkdownParser.ContentAcceptor acceptor, LarkdownParser parser) throws IOException, LarkdownParseException
    {
        ReaderTokenView view = new ReaderTokenView(reader);
        view.next();

        parseCommand(view, acceptor, parser);

        view.fastForward();
        view.fastForwardLine();
    }

    public void parseCommand(ReaderTokenView view, LarkdownParser.ContentAcceptor acceptor, LarkdownParser parser) throws IOException, LarkdownParseException
    {
        String name = view.nextIdentifier();

        LarkdownCommand command = commands.get(name);
        if (command == null)
            throw new LarkdownParseException(String.format("Unknown command: %s", name));

        parseCommand(view, command, acceptor, parser, name);
    }

    private static void parseCommand(ReaderTokenView view, LarkdownCommand command, LarkdownParser.ContentAcceptor acceptor, LarkdownParser parser, String context) throws IOException, LarkdownParseException
    {
        int c = view.peek();

        CommandOptions options = new CommandOptions();
        CommandArguments arguments = new CommandArguments();

        if (c == '[')
        {
            view.next();
            options = new CommandOptions(lookAhead(view, '[', ']', context));
            c = view.peek();
        }

        if (c == '{')
        {
            List<String> found = new ObjectArrayList<>();
            boolean exit = false;

            do
            {
                view.next();
                String thing = lookAhead(view, '{', '}', context);
                found.add(thing);

                // TODO: No whitespace handling, just pure jank.
                char c1 = view.peek();
                view.mark();
                view.fastForwardAny();
                if (view.peek() != '{')
                {
                    char c3 = view.peek();
                    view.reset();
                    char c2 = view.peek();
                    if (c1 != c2)
                        System.out.println("NOO");
                    exit = true;
                }
                // Exceeding the mark read limit should only throw an error when reset() is called
            }
            while (!exit);

            arguments = new CommandArguments(found);
        }

        command.apply(options, arguments, acceptor, parser);
    }

    /// Starts at the character immediately after the {@param start} and consumes the trailing {@param end}
    private static String lookAhead(ReaderTokenView view, char start, char end, String context) throws IOException, LarkdownParseException
    {
        StringBuilder sb = new StringBuilder();

        int level = 0;

        int c;
        do
        {
            c = view.next();
            if (c == start)
            {
                level++;
            }

            if (c == end)
            {
                if (level == 0)
                    return sb.toString();
                else
                    level--;
            }

            if (c == -1)
                break;

            sb.appendCodePoint(c);
        }
        while (true);

        throw new LarkdownParseException(String.format("Reached EOF while parsing command %s", context));
    }
}
