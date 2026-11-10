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

import com.neep.meatlib.util.StringTokenView;

public class ThordTokenView extends StringTokenView
{
    public ThordTokenView(String string, int lineOffset)
    {
        super(string, lineOffset);
    }

    public ThordTokenView(String string)
    {
        super(string);
    }

    @Override
    public boolean isIdentifier(int index, char c)
    {
        // Only used after int definition has been ruled out, so we can accept digits here.

        return c == '.' || c == '_' || c == '+' || c == '-' || Character.isAlphabetic(c) || Character.isDigit(c);
    }

    public boolean isWhitespace(char c)
    {
        return Character.isWhitespace(c);
    }

    @Override
    protected boolean isTerminator(char c)
    {
        return c == ';';
    }

    public String nextIdentifier()
    {
        fastForward();
        StringBuilder builder = new StringBuilder();

        // Everything can be an identifier!
        while (!eof() && !isWhitespace(peek()))
        {
            builder.append(next());
        }
        return builder.toString();
    }
}
