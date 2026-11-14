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

import com.neep.neepmeat.thord.parser.ThordMacro;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LexerContext
{
    public final ThordTokenView view;
    public final List<ThordToken> buffer;
    public final Map<String, ThordMacro> macros;
    public int macroLevel = 0;

    public LexerContext(ThordTokenView view)
    {
        this(view, new HashMap<>(), 0);
    }

    public LexerContext(ThordTokenView view, Map<String, ThordMacro> macros, int macroLevel)
    {
        this.view = view;

        this.buffer = new ObjectArrayList<>();
        this.macros = macros;
        this.macroLevel = macroLevel;
    }

    public void addMacro(ThordMacro macro)
    {
        macros.put(macro.name(), macro);
    }

    public ThordMacro getMacro(String name)
    {
        return macros.get(name);
    }

//    public void addAlias()
//    {
//
//    }
}
