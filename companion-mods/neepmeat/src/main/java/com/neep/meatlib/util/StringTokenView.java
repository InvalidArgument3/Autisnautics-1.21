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

package com.neep.meatlib.util;

import com.neep.meatlib.neepasm.NeepASM;
import org.jetbrains.annotations.Nullable;

public class StringTokenView extends TokenView
{
    private final String string;
    private int offset = 0;
    private final int lineOffset;
    private int currentLine = 0;

    public StringTokenView(String string, int lineOffset)
    {
        this.string = string;
        this.lineOffset = lineOffset;
    }

    public StringTokenView(String string)
    {
        this(string, 0);
    }

    public int pos()
    {
        return offset;
    }

    public int line()
    {
        return lineOffset + (int) string.substring(0, offset).lines().count() - 1;
    }

    public int linePos()
    {
        int lastLine = string.substring(0, offset).lastIndexOf('\n');
        return offset - lastLine - 1;
    }

    public char next()
    {
        if (offset >= string.length())
            return 0;

        // Return the previous value
        return string.charAt(offset++);
    }

    public void next(int n)
    {
        for (int i = 0; i < n; ++i)
            next();
    }

    public void nextLine()
    {
        while (last() != '\n' && !isTerminator(last()) && offset < string.length())
            next();

        next();
    }

    public char nextThing()
    {
        fastForward();
        return next();
    }

    public char peek() {
        return last();
    }

    public char peek(int ahead) {
        return last(ahead);
    }

    public char peekThing()
    {
        fastForward();
        return last();
    }

    public char last()
    {
        if (offset >= string.length())
            return 0;

        return string.charAt(offset);
    }

//    public String peekChars(int n)
//    {
//        int offsetCopy = offset;
//        StringBuilder builder = new StringBuilder();
//
//        while (offsetCopy <= offset + n && offsetCopy <= string.length() &&
//                !(string.charAt(offsetCopy) == '\n' || string.charAt(offsetCopy) == ';' || string.charAt(offsetCopy) == 0))
//        {
//            builder.append(string.charAt(offsetCopy++));
//        }
//        return builder.toString();
//
//    }

    public char last(int n)
    {
        if (offset + n >= string.length())
            return 0;

        return string.charAt(offset + n);
    }

    public void fastForward()
    {
        while (last() != 0 && isNotLineEnd(last()))
            next();
    }

    public String nextBlob()
    {
        fastForward();
        StringBuilder builder = new StringBuilder();
        while (last() != 0 && !isNotLineEnd(last()))
        {
            builder.append(next());
        }
        return builder.toString();
    }

    public String nextIdentifier()
    {
        fastForward();
        int index = 0;
        StringBuilder builder = new StringBuilder();
        while (isIdentifier(index, last()))
        {
            builder.append(next());
            index++;
        }
        return builder.toString();
    }

    public String nextIntegerString()
    {
        fastForward();
        StringBuilder builder = new StringBuilder();
        int index = 0;
        while (isDigit(index, last()))
        {
            builder.append(next());
            index++;
        }

        return builder.toString();
    }

    public int nextInteger() throws NeepASM.ParseException
    {
        String s = nextIntegerString();
        return parseInteger(s);
    }

    public static int parseInteger(String s) throws NeepASM.ParseException
    {
        if (s.equals("-"))
            return 0;

        int radix = 10;
        if (s.startsWith("0x"))
            radix = 16;
        else if (s.startsWith("0b"))
            radix = 2;

        // Not sure if this try-catch should be here or in the parser
        try
        {
            return Integer.parseInt(radix == 10 ? s : s.substring(2), radix);
        }
        catch (NumberFormatException e)
        {
            throw new NeepASM.ParseException(e.getMessage());
        }
    }

    @Nullable
    public String nextString()
    {
        fastForward();
        if (last() != '"' || lineEnded())
        {
            return null;
        }

        next(); // Advance over first "

        StringBuilder builder = new StringBuilder();
        while (last() != '"')
        {
            if (lineEnded())
                return null;

            builder.append(next());
        }
        next(); // Advance over last "
        return builder.toString();
    }


    public Mark mark()
    {
        return new Mark(offset, currentLine);
    }

    public Mark markPosition()
    {
        return mark();
    }

    public static boolean isDigit(char c)
    {
        return  c == '-' || Character.isDigit(c);
    }

    private static boolean isDigit(int index, char c)
    {
        // Handle hex or bin prefixes
        return (index == 1 && (c == 'x' || c == 'b')) || isDigit(c) || Character.digit(c, 16) != -1;
    }

    public boolean lineEnded()
    {
        return last() == '\n' || isTerminator(last()) || last() == 0;
    }

    protected boolean isNotLineEnd(char c)
    {
        return (c != '\n' && !isTerminator(c)) && Character.isWhitespace(c) || c == 0;
    }

    public boolean eof()
    {
        return offset >= string.length();
    }

    @Override
    public String toString()
    {
        return "TokenView: " + last();
    }

    public class Mark extends TokenView.Mark
    {
        int saved;
        int savedLine;

        public Mark(int saved, int currentLine)
        {
            this.saved = saved;
            this.savedLine = currentLine;
        }

        @Override
        public void close()
        {
            if (!commit)
            {
                offset = saved;
                currentLine = savedLine;
            }
        }
    }
}
