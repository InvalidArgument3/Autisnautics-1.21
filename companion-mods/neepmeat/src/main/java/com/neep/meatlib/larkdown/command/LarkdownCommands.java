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

package com.neep.meatlib.larkdown.command;

import com.neep.meatlib.larkdown.content.ImageLarkdownContent;
import com.neep.meatlib.larkdown.parser.Alignment;
import com.neep.meatlib.larkdown.parser.TextContentParser;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public interface LarkdownCommands
{
    TextContentParser CENTERING_PARSER = new TextContentParser(Alignment.CENTRE);

    Map<String, LarkdownCommand> DEFAULT_COMMANDS = Map.of(
            "image", (options, arguments, acceptor, parser) ->
            {
                acceptor.accept(new ImageLarkdownContent(options.getInt("width", 64), options.getInt("height", 64), options.getFloat("scale", 1), arguments.get(0)));
            },

            "centering", (options, arguments, acceptor, parser) ->
            {
                // TODO: parse formatting
//                acceptor.accept(new TextLarkdownContent(arguments.get(0), Format.NONE, Alignment.CENTRE));
                CENTERING_PARSER.parse(new BufferedReader(new StringReader(arguments.get(0))), acceptor, parser);
            }
    );

    LarkdownCommands DEFAULT = new LarkdownCommands()
    {
        @Override
        public @Nullable LarkdownCommand get(String name)
        {
            return DEFAULT_COMMANDS.get(name);
        }
    };

    @Nullable LarkdownCommand get(String name);

    class Mutable implements LarkdownCommands
    {
        private final Map<String, LarkdownCommand> map = new HashMap<>();

        public Mutable()
        {
            map.putAll(DEFAULT_COMMANDS);
        }

        public void register(String name, LarkdownCommand command)
        {
            map.put(name, command);
        }

        @Override
        public @Nullable LarkdownCommand get(String name)
        {
            return map.get(name);
        }
    }
}
