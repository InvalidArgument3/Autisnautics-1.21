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

package com.neep.neepmeat.thord.token;

import com.google.common.collect.Lists;
import com.neep.meatlib.util.StringTokenView;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.plc.instruction.Argument;
import com.neep.neepmeat.thord.Keyword;
import com.neep.neepmeat.thord.parser.ThordDictionary;
import com.neep.neepmeat.thord.parser.ThordMacro;
import com.neep.neepmeat.thord.parser.node.WorldTargetAliasToken;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class ThordLexer
{
    private final ThordDictionary dictionary;
    private final Set<String> inlineOperations;

    public ThordLexer(ThordDictionary dictionary, Set<String> inlineOperations)
    {
        this.dictionary = dictionary;
        this.inlineOperations = inlineOperations;
    }

    public List<ThordToken> tokenise(String source) throws NeepASM.ProgramBuildException
    {
        List<ThordToken> tokens = new ObjectArrayList<>();
        ThordTokenView view = new ThordTokenView(source);

        try
        {
            tokenise(new LexerContext(view), tokens);
        }
        catch (NeepASM.ParseException e)
        {
            throw new NeepASM.ProgramBuildException(view.line(), view.linePos(), e.getMessage());
        }

        return tokens;
    }

    private void tokenise(LexerContext context, List<ThordToken> tokens) throws NeepASM.ParseException
    {
        while (!context.view.eof())
        {
            ThordToken token = nextToken(context);
            tokens.add(token);
        }
    }

    @Nullable
    public ThordToken nextToken(LexerContext context) throws NeepASM.ParseException
    {
        var buffer = context.buffer;
        var view = context.view;

        if (!buffer.isEmpty())
            return buffer.remove(0);

        view.fastForward();

        char c = view.peek();

        boolean newLine = view.pos() == 0;

        // Jank for allowing inline instructions after new lines
        if (view.isWhitespace(c) && c == '\n')
        {
            view.next();
            view.fastForward();
            c = view.peek();
            newLine = true;
        }

        if (view.isWhitespace(c) && c != '\n')
        {
            view.next();
        }
        else if (c == '#')
        {
            // Line comment
            while (view.peek() != '\n' && !view.eof())
            {
                view.next();
            }
        }
        else if (c == '(')
        {
            char c1;
            do
            {
                if (view.eof())
                    throw new NeepASM.ParseException("reached EOF during word definition");

                c1 = view.next();
            }
            while (c1 != ')');
        }
        else if (c == ';')
        {
            view.next();
            return new ScopeEndToken(view.line());
        }
        else if (c == '%' && view.peek(1) == ':')
        {
            parseMacro(view, context);
        }
        else if (c == '%' && view.peek(1) == 'a')
        {
            return parseAlias(view, context);
        }
        else if (c == '@' && view.peek(1) == '(')
        {
            return parseWorldTarget(view, context);
        }
        else if (c == '"')
        {
            return parseString(view, context);
        }
        else if (c == '.' && !view.isWhitespace(view.peek(1)) && view.peek(1) != 0) // Check for EOF too
        {
            // Consume the clump of text up to the line or next ; for the instruction parser
            view.next();

            String identifier = view.nextIdentifier();
            return processInline(identifier, view);
        }
        else if ((c == '-' && StringTokenView.isDigit(view.peek(1))) || StringTokenView.isDigit(c) && !isIdentifier(view))
        {
             return new IntLiteralToken(view.nextInteger(), view.line());
        }
        else if (c == '\"')
        {
            view.next();
            // TODO: String literal
        }
        else
        {
            return processIdentifier(newLine, view, context);
        }

        return null;
    }

    private @NotNull ThordToken parseString(ThordTokenView view, LexerContext context)
    {
        return new StringToken(view.line(), view.nextString());
    }

    private @NotNull CoordsToken parseWorldTarget(ThordTokenView view, LexerContext context) throws NeepASM.ParseException
    {
        view.next(2);
        b1:
        {
            int x, y, z;
            Direction direction = Direction.UP;

            if (StringTokenView.isDigit(view.peekThing()))
                x = view.nextInteger();
            else
                break b1;

            if (StringTokenView.isDigit(view.peekThing()))
                y = view.nextInteger();
            else break b1;

            if (StringTokenView.isDigit(view.peekThing()))
                z = view.nextInteger();
            else break b1;

            // Direction is optional
            if (Character.isAlphabetic(view.peekThing()))
            {
                // A dirty hack to allow the omission of a space between the direction and the )
                StringBuilder sb = new StringBuilder();
                while (view.peek() != ')' && !view.eof())
                    sb.append(view.next());

                direction = NeepAsmParser.parseDirection(sb.toString());
                if (direction == null)
                    direction = Direction.UP; // Unrecognised text will throw an exception
            }

            if (view.nextThing() == ')')
            {
                return new CoordsToken(view.line(), new Argument(new BlockPos(x, y, z), direction));
            }
        }
        throw new NeepASM.ParseException("malformed target\n Targets should be of the form '@(<x> <y> <z> <direction>)");
    }

    private void parseMacro(ThordTokenView view, LexerContext context) throws NeepASM.ParseException
    {
        view.fastForward();

        view.next(2);
        StringBuilder macroText = new StringBuilder();

        String name = view.nextIdentifier();
        if (name.isEmpty())
            throw new NeepASM.ParseException("expected macro name");

        int startLine = view.line();

        List<String> parameters = Lists.newArrayList();
        view.fastForward();
        while (view.isIdentifier(0, view.peek()))
        {
            String parameter = view.nextIdentifier();
            parameters.add(parameter);
            view.fastForward();
        }

        if (!view.lineEnded() && !NeepAsmParser.isComment(view))
            throw new NeepASM.ParseException("unexpected token");

        while (!view.eof())
        {
            if (view.peek() == '%')
            {
                try (var entry = view.mark())
                {
                    view.next();
                    String id = view.nextIdentifier();
                    if (id.equals(";"))
                    {
                        entry.commit();
                        context.addMacro(new ThordMacro(name, parameters, macroText.toString(), startLine));
                        return;
                    }
                }
            }

            macroText.append(view.peek());
            view.next();
        }

        throw new NeepASM.ParseException("reached end of file while parsing macro '" + name + "'");
    }

    private ThordToken parseAlias(ThordTokenView view, LexerContext context) throws NeepASM.ParseException
    {
        char c;
        do
        {
            c = view.next();
            if (view.eof())
                throw new NeepASM.ParseException("expected alias name");
        }
        while (!view.isWhitespace(c));

        view.fastForward();

        String name = view.nextIdentifier();
        if (name.isEmpty())
            throw new NeepASM.ParseException("expected value after alias");

        if (view.nextThing() != '=')
            throw new NeepASM.ParseException("expected = after alias name");

        view.fastForward();
        CoordsToken token = parseWorldTarget(view, context);
        return new WorldTargetAliasToken(token.line(), name, token.argument());

//        throw new NeepASM.ParseException("invalid value for alias '" + name + "'");
    }

    private ThordToken processInline(String identifier, ThordTokenView view)
    {
        StringBuilder sb = new StringBuilder();
        while (!view.lineEnded())
        {
            char c1 = view.next();
            sb.append(c1);
        }

        // Advance past ;
        if (view.isTerminator(view.peek()))
            view.next();

        return new InlineToken(identifier, sb.toString(), view.line());
    }

    private ThordToken processIdentifier(boolean newLine, ThordTokenView view, LexerContext context) throws NeepASM.ParseException
    {
        String identifier = view.nextIdentifier();

        if (!identifier.isEmpty())
        {
            for (Keyword keyword : Keyword.values())
            {
                if (keyword.matches(identifier))
                {
                    return new KeywordToken(keyword, view.line());
                }
            }

            // Prioritise built in words over inline NEEPASM
//            if ((dictionary.contains(identifier) || !inlineOperations.contains(identifier)))
            ThordMacro macro = context.getMacro(identifier);
            if (macro != null)
            {
                return expandMacro(context, macro);
            }
            else if (newLine && !dictionary.contains(identifier) && inlineOperations.contains(identifier))
            {
                return processInline(identifier, view);
            }
            else
            {
                return new IdentifierToken(identifier, view.line());
            }
        }

        return null;
    }

    @Nullable
    private ThordToken expandMacro(LexerContext context, ThordMacro macro) throws NeepASM.ParseException
    {
        String expanded = macro.expand(context.view);
        if (context.macroLevel > 8)
            throw new NeepASM.ParseException("reached maximum macro nesting depth");
        tokenise(new LexerContext(new ThordTokenView(expanded, macro.getStartLine()), context.macros, context.macroLevel + 1), context.buffer);

        return null;
    }

    // Cursed
    boolean isIdentifier(ThordTokenView view)
    {
        // gggGGGggGGGGGGgg
        try (var ignore = view.mark())
        {
            String s = view.nextIdentifier();

            // Special stupid handling for hfhfhbhbfhbh
            if (s.equals("-"))
                return true;

            // Throws an exception if invalid.
            StringTokenView.parseInteger(s);

            return false;
        }
        catch (NeepASM.ParseException e)
        {
            return true;
        }

//        if (Character.isDigit(c1) && (c0 == '+' || c0 == '-'))
//            return false;
//
//        return ThirdTokenView.isThirdIdentifier(0, c0) && ThirdTokenView.isThirdIdentifier(1, c1);
    }

    public boolean hasNext(LexerContext context)
    {
        return !context.view.eof() || !context.buffer.isEmpty();
    }
}
