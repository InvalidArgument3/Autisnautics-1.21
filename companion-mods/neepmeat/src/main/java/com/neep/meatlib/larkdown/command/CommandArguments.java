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

import com.neep.meatlib.larkdown.LarkdownParseException;
import com.neep.meatlib.util.StringTokenView;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public class CommandArguments
{
    private final List<String> arguments = new ObjectArrayList<>();

    public CommandArguments()
    {

    }

    public CommandArguments(List<String> string)
    {
        arguments.addAll(string);
    }

    private static String readCommaSeparated(StringTokenView view)
    {
        StringBuilder sb = new StringBuilder();
        char c;
        do
        {
            c = view.next();
            if (c != ',')
                sb.append(c);
        }
        while (c != ',' && !view.eof());

        return sb.toString().strip();
    }

    public String get(int i) throws LarkdownParseException
    {
        try
        {
            return arguments.get(i);
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new LarkdownParseException(e.getMessage());
        }
    }
}
