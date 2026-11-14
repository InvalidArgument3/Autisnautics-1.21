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

import java.io.IOException;
import java.io.StringReader;

public class LarkdownParserTest
{
    public static void main(String[] args)
    {
        String markdown = """
                ---
                id: the_thingy
                ---
                # Heading 1
                ## Heading 2
                
                ### Heading 3
                
                **Some bold text**
                
                some `code` here
                
                \\image[width=32,height=98,scale=0.2]{neepmeat:guide/images/transducer_usage.png}
                The text begins! **Some bold* text** and a little bit of *italic* text NOW IS THE END OF THE TEXT.
                """;


        StringReader reader = new StringReader(markdown);

        LarkdownParser parser = new LarkdownParser(reader);
        try
        {
            var contents = parser.getContents();
            var preamble = parser.getPreamble();

            for (LarkdownContent content : contents)
                System.out.println(content);

            System.out.println();
            System.out.println(preamble);
        }
        catch (IOException | LarkdownParseException e)
        {
            throw new RuntimeException(e);
        }
    }
}
