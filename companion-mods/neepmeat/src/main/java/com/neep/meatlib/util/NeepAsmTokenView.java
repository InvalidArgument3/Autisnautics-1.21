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

public class NeepAsmTokenView extends StringTokenView
{
    public NeepAsmTokenView(String string, int lineOffset)
    {
        super(string, lineOffset);
    }

    public NeepAsmTokenView(String string)
    {
        super(string);
    }

    @Override
    protected boolean isTerminator(char c)
    {
        return c == ';';
    }
}
