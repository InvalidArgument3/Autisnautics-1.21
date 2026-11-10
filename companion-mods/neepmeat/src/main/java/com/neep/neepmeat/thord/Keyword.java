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

package com.neep.neepmeat.thord;

import java.util.Locale;

public enum Keyword
{
    WORD(":"),
    WORD_NONAME(":noname"),
    IMMEDIATE_WORD(":i"),
    LABEL("label"),
//    LEFT_BRACKET("["),
//    RIGHT_BRACKET("]"),
    VARIABLE("variable"),
    ARRAY("array"), // Needs to be a keyword so that the amount of allocated memory is known at compile time
    POSTPONE("postpone"),
    ;

    private final String lowerName;

    Keyword(String name)
    {
        this.lowerName = name;
    }

    // Allows multiple synonyms of the same keyword to be added later
    // Yay! COBOL!
    public boolean matches(String value)
    {
        return value.toLowerCase(Locale.ROOT).equals(asString());
    }

    public String asString()
    {
        return lowerName;
    }
}
