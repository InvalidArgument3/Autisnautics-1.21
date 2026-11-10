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

import com.neep.meatlib.util.TokenView;

import java.io.BufferedReader;
import java.io.IOException;

public class ReaderTokenView extends TokenView
{
    private final BufferedReader reader;

    // Looks ahead one character for mark-free peeking
    private int peeked;
    private int markedPeek;

    public ReaderTokenView(BufferedReader reader) throws IOException
    {
        this.reader = reader;
        peeked = reader.read();
    }

    public void mark() throws IOException
    {
        reader.mark(1000);
        markedPeek = peeked;
    }

    public void reset() throws IOException
    {
        reader.reset();
        peeked = markedPeek;
    }

    public String nextIdentifier() throws IOException
    {
        fastForward();
        int index = 0;
        StringBuilder builder = new StringBuilder();
        while (isIdentifier(index, peek()))
        {
            builder.appendCodePoint(next());
            index++;
        }
        return builder.toString();
    }

    public int next() throws IOException
    {
        int c = peeked;
        peeked = reader.read();
        return c;
    }

    public char peek() throws IOException
    {
        return (char) peeked;
    }

    public void fastForward() throws IOException
    {
        while (peek() != 0 && isNotLineEnd(peek()))
            next();
    }

    public void fastForwardAny() throws IOException
    {
        while (peek() != 0 && Character.isWhitespace(peek()))
            next();
    }

    public void fastForwardLine() throws IOException
    {
        while (peek() != 0 && isNotLineEnd(peek()))
            next();
    }

    private boolean isNotLineEnd(char c)
    {
        return (c != '\n' && !isTerminator(c)) && Character.isWhitespace(c) || c == 0;
    }

//        public Mark mark(int bufferSize) throws IOException
//        {
//            return new Mark(bufferSize);
//        }
//
//        public class Mark extends TokenView.Mark
//        {
//            public Mark(int bufferSize) throws IOException
//            {
//                reader.mark(bufferSize);
//            }
//
//            @Override
//            public void close() throws Exception
//            {
//                if (!commit)
//                    reader.reset();
//            }
//        }
}
