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
import com.neep.meatlib.larkdown.content.TextLarkdownContent;

import java.io.BufferedReader;
import java.io.IOException;

public class CommandTextContentParser implements LarkdownParser.ContentParser
{

    @Override
    public boolean matches(String line)
    {
        return true;
    }

    @Override
    public void parse(BufferedReader reader, LarkdownParser.ContentAcceptor acceptor, LarkdownParser parser) throws IOException, LarkdownParseException
    {
        StringBuilder sb = new StringBuilder();
        ReaderTokenView view = new ReaderTokenView(reader);
        int cp;
        do
        {
            cp = view.next();

            if (cp == '\\')
            {
                // Append last item
                acceptor.accept(new TextLarkdownContent(sb.toString(), Format.NONE, Alignment.LEFT));
                sb = new StringBuilder();

//                LineCommandParser.parseCommand(view, acceptor);

                cp = view.peek();
            }

            if (cp != -1)
                sb.appendCodePoint(cp);

            // Empty line indicates break
            if (cp == '\n')
            {
                acceptor.accept(new TextLarkdownContent(sb.toString(), Format.NONE, Alignment.LEFT));
                return;
            }
        }
        while (cp != -1);
    }

}
