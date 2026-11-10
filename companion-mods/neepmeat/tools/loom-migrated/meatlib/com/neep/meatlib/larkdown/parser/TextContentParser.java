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

import com.neep.meatlib.larkdown.LarkdownParser;
import com.neep.meatlib.larkdown.content.TextLarkdownContent;
import com.neep.meatlib.util.StringTokenView;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

public class TextContentParser implements LarkdownParser.ContentParser
{
    private final Alignment alignment;

    public TextContentParser(Alignment alignment)
    {
        this.alignment = alignment;
    }

    @Override
    public boolean matches(String line)
    {
        return true;
    }

    // Incredibly cursed parsing logic that *mostly* works. It works well enough, anyway.
    @Override
    public void parse(BufferedReader reader, LarkdownParser.ContentAcceptor acceptor, LarkdownParser parser) throws IOException
    {
        String text = reader.readLine();

        if (text.isBlank())
        {
            acceptor.accept(new TextLarkdownContent(List.of(new TextLarkdownContent.Entry(" ", Format.NONE)), alignment));
            return;
        }

        StringTokenView view = new StringTokenView(text);
        List<TextLarkdownContent.Entry> entries = new ObjectArrayList<>();

        StringBuilder chunk = new StringBuilder();
        do
        {
            for (var format : Format.values())
            {
                if (format.signifier == null)
                    continue;

                boolean matched = true;
                for (int i = 0; i < format.signifier.length(); ++i)
                {
                    char c1 = view.peek(i);
                    char c2 = format.signifier.charAt(i);

                    if (c1 != c2)
                    {
                        matched = false;
                        break;
                    }
                }

                if (matched)
                {
                    view.next(format.signifier.length());
                    String ahead = lookAhead(view, format.signifier);
                    if (ahead != null)
                    {
                        entries.add(new TextLarkdownContent.Entry(chunk.toString(), Format.NONE));
                        entries.add(new TextLarkdownContent.Entry(ahead, format));
                        chunk = new StringBuilder();
                        break;
                    }
                }
            }

            char c = view.peek();
            if (c != 0)
                chunk.append(c);
            view.next();
        }
        while (!view.eof());

        if (!chunk.isEmpty())
            entries.add(new TextLarkdownContent.Entry(chunk.toString(), Format.NONE));

        acceptor.accept(new TextLarkdownContent(entries, alignment));
    }

    @Nullable
    private String lookAhead(StringTokenView view, String signifier)
    {
        StringBuilder sb = new StringBuilder();
        try (var mark = view.mark())
        {
            char c;
            do
            {
                c = view.peek();

                if (c == signifier.charAt(0))
                {
                    boolean matched = true;
                    for (int i = 0; i < signifier.length(); ++i)
                    {
                        char c1 = view.peek(i);
                        if (c1 != signifier.charAt(i))
                            matched = false;
                    }

                    if (matched)
                    {
                        mark.commit();
                        view.next(signifier.length());
                        return sb.toString();
                    }
                }

                sb.append(c);
                view.next();
            }
            while (!view.eof());
        }

        return null;
    }
}
