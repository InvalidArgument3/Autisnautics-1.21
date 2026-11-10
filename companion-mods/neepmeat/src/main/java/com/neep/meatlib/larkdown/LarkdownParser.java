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

package com.neep.meatlib.larkdown;

import com.neep.meatlib.larkdown.command.LarkdownCommands;
import com.neep.meatlib.larkdown.content.BulletLarkdownContent;
import com.neep.meatlib.larkdown.content.CodeLarkdownContent;
import com.neep.meatlib.larkdown.content.HeadingLarkdownContent;
import com.neep.meatlib.larkdown.parser.Alignment;
import com.neep.meatlib.larkdown.parser.LineCommandParser;
import com.neep.meatlib.larkdown.parser.TextContentParser;
import com.neep.meatlib.util.StringTokenView;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class LarkdownParser
{
    // Entries have higher precedence than ones that follow them.
    private final List<ContentParser> parsers;

    private boolean parsed = false;

    @Nullable private List<LarkdownContent> contents;
    private Map<String, String> preamble = new HashMap<>();

    private final Reader inputReader;

    public LarkdownParser(Reader stream)
    {
        this(stream, List.of(
                new HeadingContentParser(),
                new BulletContentParser(),
                new CodeContentParser(),
                new LineCommandParser(LarkdownCommands.DEFAULT),
                new TextContentParser(Alignment.LEFT)
                //        new CommandTextContentParser()
        ));
    }

    public LarkdownParser(Reader stream, List<ContentParser> parsers)
    {
        this.inputReader = stream;
        this.parsers = parsers;
    }

    public void parse() throws IOException, LarkdownParseException
    {
        parsed = true;

        contents = new ObjectArrayList<>();

        try (BufferedReader reader = readLines())
        {
            parseWhole(reader);
        }
    }

    private void parseWhole(BufferedReader reader) throws IOException, LarkdownParseException
    {
        // Read preamble
        reader.mark(1000);
        String firstLine = reader.readLine();
        reader.reset();

        if (firstLine.startsWith("---"))
        {
            reader.readLine();
            preamble = readPreamble(reader);
        }

        parseSection(reader, this::acceptContent);
    }

    public void parseSection(BufferedReader reader, ContentAcceptor acceptor) throws IOException, LarkdownParseException
    {
        String line;
        do
        {
            // This limit is not ideal
            reader.mark(10000);
            line = reader.readLine();
            reader.reset();

            if (line != null)
            {
                for (ContentParser parser : parsers)
                {
                    if (parser.matches(line))
                    {
                        parser.parse(reader, acceptor, this);
                        break;
                    }
                }
            }
        }
        while (line != null);
    }

    private void acceptContent(LarkdownContent content)
    {
//        if (!contents.isEmpty())
//        {
//            LarkdownContent lastContent = contents.get(contents.size() - 1);
//
//            LarkdownContent combined = lastContent.combine(content);
//            if (combined != null)
//            {
//                contents.set(contents.size() - 1, combined);
//                return;
//            }
//        }

        contents.add(content);
    }

    public Map<String, String> getPreamble() throws IOException, LarkdownParseException
    {
        if (!parsed)
            parse();

        return preamble;
    }

    public List<LarkdownContent> getContents() throws IOException, LarkdownParseException
    {
        if (!parsed)
            parse();

        return contents;
    }

    private Map<String, String> readPreamble(BufferedReader reader) throws IOException
    {
        Map<String, String> preamble = new HashMap<>();

        String line;
        do
        {
            line = reader.readLine();

            if (line != null)
            {
                if (line.startsWith("---"))
                    return preamble;

                StringTokenView view = new StringTokenView(line);

                var pair = readKeyValue(view);

                if (pair != null)
                    preamble.put(pair.key(), pair.value());

            }
        }
        while (line != null);

        return preamble;
    }

    @Nullable
    private Pair<String, String> readKeyValue(StringTokenView view)
    {
        try (var entry = view.mark())
        {
            String key = view.nextIdentifier();
            if (!key.isEmpty())
            {
                if (view.nextThing() == ':')
                {
                    StringBuilder sb = new StringBuilder();
                    while (!view.lineEnded())
                    {
                        sb.append(view.next());
                    }

                    entry.commit();
                    return Pair.of(key, sb.toString().strip());
                }
            }
        }

        return null;
    }

    private BufferedReader readLines() throws IOException
    {
        return new BufferedReader(this.inputReader);
    }

    public interface Visitor
    {
        void visit(LarkdownContent content);

        class Builder
        {
            private final Map<Class<? extends LarkdownContent>, ContentFunction<LarkdownContent>> map = new IdentityHashMap<>();

            private Builder() { }

            public <T extends LarkdownContent> Builder type(Class<T> clazz, ContentFunction<T> function)
            {
                // Hrmm
                map.put(clazz, (ContentFunction<LarkdownContent>) function);
                return this;
            }

            public Visitor build()
            {
                return content -> map.getOrDefault(content.getClass(), c -> {}).apply(content);
            }
        }

        static Builder builder()
        {
            return new Builder();
        }
    }

    @FunctionalInterface
    public interface ContentFunction<T extends LarkdownContent>
    {
        void apply(T content);
    }

    public interface ContentAcceptor
    {
        void accept(LarkdownContent content);
    }

    public interface ContentParser
    {
        boolean matches(String line);

        void parse(BufferedReader reader, ContentAcceptor acceptor, LarkdownParser parser) throws IOException, LarkdownParseException;
    }

    public static class HeadingContentParser implements ContentParser
    {
        @Override
        public boolean matches(String line)
        {
            return line.startsWith("#");
        }

        @Override
        public void parse(BufferedReader reader, ContentAcceptor acceptor, LarkdownParser parser) throws IOException
        {
            int level = 0;
            reader.mark(1);

            int cp = reader.read();
            while (cp == '#')
            {
                level++;
                cp = reader.read();
            }

            String text = reader.readLine();

            acceptor.accept(new HeadingLarkdownContent(text, level));
        }
    }

    public static class BulletContentParser implements ContentParser
    {
        @Override
        public boolean matches(String line)
        {
            if (line.length() > 3)
            {
                char c0 = line.charAt(0);
                char c1 = line.charAt(1);
                return (c0 == '-' || c0 == '+')
                        && (Character.isWhitespace(c1));
            }

            return false;
        }

        @Override
        public void parse(BufferedReader reader, ContentAcceptor acceptor, LarkdownParser parser) throws IOException
        {
            for (int i = 0; i < 2; ++i)
            {
                reader.read();
            }

            acceptor.accept(new BulletLarkdownContent(reader.readLine()));
        }
    }

    public static class CodeContentParser implements ContentParser
    {
        @Override
        public boolean matches(String line)
        {
            return line.startsWith("```");
        }

        @Override
        public void parse(BufferedReader reader, ContentAcceptor acceptor, LarkdownParser parser) throws IOException
        {
            String line = reader.readLine(); // Skip over first line (should only contain ```)

            List<String> codeLines = new ObjectArrayList<>();

            do
            {
                line = reader.readLine();

                if (line != null && !line.startsWith("```"))
                    codeLines.add(line);
            }
            while (line != null && !line.startsWith("```"));

            acceptor.accept(new CodeLarkdownContent(codeLines));
        }
    }
}
