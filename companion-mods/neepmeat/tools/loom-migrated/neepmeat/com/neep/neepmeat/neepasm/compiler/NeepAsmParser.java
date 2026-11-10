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
import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.meatlib.util.StringTokenView;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.alias.ParsedAlias;
import com.neep.neepmeat.neepasm.compiler.alias.ParsedArgumentAlias;
import com.neep.neepmeat.neepasm.compiler.parser.InstructionParser;
import com.neep.neepmeat.neepasm.compiler.parser.ParsedFunctionCallInstruction;
import com.neep.neepmeat.neepasm.compiler.parser.ParsedInstruction;
import com.neep.neepmeat.neepasm.compiler.parser.ParsedMacro;
import com.neep.neepmeat.neepasm.program.KeyValue;
import com.neep.neepmeat.neepasm.program.Label;
import com.neep.neepmeat.plc.instruction.Argument;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import com.neep.neepmeat.thord.token.StringToken;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * It's the worst lexer/parser ever!
 * We don't need all those silly things like operator precedence, nested scopes blah blah blah. It's all bloat.
 * Abstract syntax tree? Never heard of her.
 */
public class NeepAsmParser
{
    private final Map<String, InstructionProvider> instructionMap;

    private ParsedSource parsedSource = new ParsedSource();
    private int line = 0;

    public NeepAsmParser(Map<String, InstructionProvider> instructionMap)
    {
        this.instructionMap = instructionMap;
    }

    public static boolean isSimplePattern(String identifier)
    {
        return identifier.matches("^[a-zA-Z0-9_:]+$");
    }

    public static String convertToRegex(String notGlob)
    {
        return notGlob.replace("*", ".*");
    }

    public ParsedSource parse(String source) throws NeepASM.ProgramBuildException
    {
        parsedSource = new ParsedSource();

        NeepAsmTokenView view = new NeepAsmTokenView(source);

        try
        {
            this.line = 0;
            while (!view.eof())
            {
                parseLine(view);
                view.nextLine();
                this.line++;
            }

            parsedSource.instruction(((s, program) -> Instruction.EMPTY), -1);

            for (var func : parsedSource.functions())
            {
                func.expand(parsedSource);
            }
        }
        catch (NeepASM.ParseException e)
        {
            throw new NeepASM.ProgramBuildException(view.line(), view.linePos(), e.getMessage());
        }
        return parsedSource;
    }

    public void parseInstructionOrMacro(InstructionAcceptor parsedSource, NeepAsmTokenView view, String prevToken, @Nullable String scope) throws NeepASM.ParseException
    {
        ParsedMacro macro = parsedSource.findMacro(prevToken);
        if (macro != null)
        {
            macro.expand(view, parsedSource, this, new HashSet<>());
        }
        else
        {
            ParsedInstruction instruction = parseInstruction(view, scope);
            if (instruction != null)
                parsedSource.instruction(instruction, view.line());
        }
    }

    public void parseLine(NeepAsmTokenView view) throws NeepASM.ParseException
    {
        if (view.peekThing() == '%')
        {
            view.next();
            String id = view.nextIdentifier();
            switch (id)
            {
                case "func" ->
                {
                    parseFunction(view);
                    return;
                }
                case "macro" ->
                {
                    parseMacro(view);
                    return;
                }
                case "alias" ->
                {
                    parseAlias(view);
                    return;
                }
            }
            throw new NeepASM.ParseException("unexpected directive '" + id + "'");
        }

        String token;
        try (var entry = view.mark())
        {
            token = view.nextIdentifier();
            char follow = view.nextThing();
            if (follow == ':')
            {
                parsedSource.label(new Label(token, parsedSource.size()));
                assureLineEnd(view);

                entry.commit();
            }
        }

        parseInstructionOrMacro(parsedSource, view, token, null);
    }

    private void parseMacro(NeepAsmTokenView view) throws NeepASM.ParseException
    {
        StringBuilder macroText = new StringBuilder();
        String name = view.nextIdentifier();
        if (name.isEmpty())
            throw new NeepASM.ParseException("expected macro name");

        int startLine = view.line();

        List<String> parameters = Lists.newArrayList();
        view.fastForward();
        while (view.isIdentifier(0, view.peek()))
        {
            parameters.add(parseMacroParameter(view));
            view.fastForward();
        }

        if (!view.lineEnded() && !isComment(view))
            throw new NeepASM.ParseException("unexpected token");

        while (!view.eof())
        {
            if (view.peek() == '%')
            {
                try (var entry = view.mark())
                {
                    view.next();
                    String id = view.nextIdentifier();
                    if (id.equals("end"))
                    {
                        entry.commit();
                        parsedSource.macro(new ParsedMacro(name, parameters, macroText.toString(), startLine));
                        return;
                    }
//                    else
//                        throw new NeepASM.ParseException("expected %end");
                }
            }

            macroText.append(view.peek());
            view.next();
        }

        throw new NeepASM.ParseException("reached end of file while parsing macro '" + name + "'");
    }

    private String parseMacroParameter(NeepAsmTokenView view)
    {
        view.fastForward();
        return view.nextIdentifier();
    }

    private void parseAlias(NeepAsmTokenView view) throws NeepASM.ParseException
    {
        String name = view.nextIdentifier();
        if (name.isEmpty())
            throw new NeepASM.ParseException("expected value after alias");

        if (view.nextThing() != '=')
            throw new NeepASM.ParseException("expected = after alias name");

        Argument argument;
        if ((argument = parseWorldTarget(view)) != null)
        {
            parsedSource.alias(new ParsedArgumentAlias(name, argument));
            return;
        }

        throw new NeepASM.ParseException("invalid value for alias '" + name + "'");
    }

    private void parseFunction(NeepAsmTokenView view) throws NeepASM.ParseException
    {
        String name = view.nextIdentifier();
        if (name.isEmpty())
            throw new NeepASM.ParseException("expected function name");

        if (!view.lineEnded() && !isComment(view))
            throw new NeepASM.ParseException("unexpected token");

        view.nextLine();

        ParsedFunction function = new ParsedFunction(name, parsedSource::findMacro);
        view.fastForward();
        while (view.peekThing() != '%')
        {
            parseFunctionLine(function, view);
            view.nextLine();

            if (view.eof())
                throw new NeepASM.ParseException("reached end of file while parsing function '" + name + "'");
        }
        view.next();
        if (!view.nextIdentifier().equals("end"))
            throw new NeepASM.ParseException("in '" + name + "': directives not allowed in function");

        parsedSource.function(function);
    }

    private void parseFunctionLine(ParsedFunction function, NeepAsmTokenView view) throws NeepASM.ParseException
    {
        String token;
        try (var entry = view.mark())
        {
            token = view.nextIdentifier();
            char follow = view.nextThing();
            if (follow == ':')
            {
                function.label(function.mangleLabel(token, function.size()));
                assureLineEnd(view);
                entry.commit();
                return;
            }
        }
        parseInstructionOrMacro(function, view, token, function.name());
    }

    @Nullable
    public ParsedInstruction parseInstruction(NeepAsmTokenView view, @Nullable String scope) throws NeepASM.ParseException
    {
        view.fastForward();
        if (view.lineEnded() || isComment(view))
            return null;

        String id = view.nextIdentifier();

        InstructionProvider provider = readInstruction(id);
        if (provider != null)
        {
            InstructionParser parser = provider.getParser();
            return parser.parse(view, this, scope);
        }

        ParsedFunction function = parsedSource.findFunction(id);
        if (function != null)
            return new ParsedFunctionCallInstruction(id, view);

        throw new NeepASM.ParseException("unrecognised operation '" + id + "'");
    }

    @Nullable
    private InstructionProvider readInstruction(String id)
    {
        return instructionMap.get(id.toLowerCase(Locale.ROOT));
    }

    @Nullable
    public Argument parseWorldTarget(NeepAsmTokenView view) throws NeepASM.ParseException
    {
        try (var entry = view.mark())
        {
            if (view.peekThing() == '$')
            {
                view.next();
                entry.commit();

                String name = view.nextIdentifier();
                if (name.isEmpty())
                    throw new NeepASM.ParseException("expected alias name");

                ParsedAlias alias = parsedSource.findAlias(name);
                if (alias == null)
                    throw new NeepASM.ParseException("alias '" + name + "' not found");

//                if (alias.type() != ParsedAlias.Type.ARGUMENT)
//                    throw new NeepASM.ParseException("alias '" + name + "' is not a world position");

                return ((ParsedArgumentAlias) alias).argument();
            }
            else
            {
                entry.commit();
                var other = parseWorldTarget1(view);

                if (other == null)
                    throw new NeepASM.ParseException("malformed target\n Targets should be of the form '@(<x> <y> <z> <direction>)");

                return other;
            }
        }
    }

    public static Argument parseWorldTarget1(StringTokenView view) throws NeepASM.ParseException
    {
        if (view.peekThing() == '@')
        {
            view.next();

            b1: if (view.nextThing() == '(')
            {
                int x, y, z;
                Direction direction = Direction.UP;

                if (isDigit(view.peekThing()))
                    x = view.nextInteger();
                else break b1;

                if (isDigit(view.peekThing()))
                    y = view.nextInteger();
                else break b1;

                if (isDigit(view.peekThing()))
                    z = view.nextInteger();
                else break b1;

                // Direction is optional
                if (Character.isAlphabetic(view.peekThing()))
                {
                    direction = parseDirection(view);
                    if (direction == null)
                        direction = Direction.UP; // Unrecognised text will throw an exception
                }

                if (view.nextThing() == ')')
                {
                    return new Argument(new BlockPos(x, y, z), direction);
                }
            }
        }

        return null;
    }

    /**
     * @return Direction or null if none found.
     * @throws NeepASM.ParseException if direction name is invalid
     */
    @Nullable
    public static Direction parseDirection(StringTokenView view) throws NeepASM.ParseException
    {
        String name = view.nextIdentifier();
        return parseDirection(name);
    }

    @Nullable
    public static Direction parseDirection(String name) throws NeepASM.ParseException
    {
        if (!name.isEmpty())
        {
            Direction direction = directionByName(name);
            if (direction == null)
            {
                throw new NeepASM.ParseException("no such direction '" + name + "'\n Directions are specified with the full name or first letter (north or n)");
            }
            return direction;
        }
        return null;
    }

    /**
     * @param name Name of the direction (full name or first letter)
     * @return Direction or null if the name is invalid
     */
    @Nullable
    public static Direction directionByName(String name)
    {
        name = name.toLowerCase(Locale.ROOT);
        Direction d = Direction.byName(name);
        if (d == null)
        {
            d = switch (name)
            {
                case "n" -> Direction.NORTH;
                case "e" -> Direction.EAST;
                case "s" -> Direction.SOUTH;
                case "w" -> Direction.WEST;
                case "u" -> Direction.UP;
                case "d" -> Direction.DOWN;
                default -> null;
            };
        }
        return d;
    }

    @Nullable
    public KeyValue parseKV(NeepAsmTokenView view) throws NeepASM.ParseException
    {
        try (var entry = view.mark())
        {
            String key = view.nextIdentifier();
            if (!key.isEmpty())
            {
                if (view.nextThing() == '=')
                {
                    String val = view.nextIdentifier();
                    if (!val.isEmpty())
                    {
                        entry.commit();
                        return new KeyValue(key, val);
                    }
                }
                throw new NeepASM.ParseException("malformed key-value pair");
            }
        }
        return null;
    }

    public void assureLineEnd(NeepAsmTokenView view) throws NeepASM.ParseException
    {
        view.fastForward();
        boolean b1 = isComment(view);
        boolean b2 = view.lineEnded();
        if (!isComment(view) && !view.lineEnded())
            throw new NeepASM.ParseException("unexpected token '" + view.nextBlob() + "'");
    }

    private static boolean isDigit(char c)
    {
        return c == '-' || Character.isDigit(c);
    }

    public static boolean isComment(StringTokenView view)
    {
        try (var ignored = view.mark())
        {
            return view.nextThing() == '#';
        }
    }

    public ParsedSource getParsedSource()
    {
        return parsedSource;
    }
}