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

package com.neep.neepmeat.neepasm.compiler.parser;

import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.neepmeat.api.plc.program.MutableProgram;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.LabelLookup;
import com.neep.neepmeat.neepasm.compiler.ParsedFunction;
import com.neep.neepmeat.neepasm.compiler.ParsedSource;
import net.minecraft.server.level.ServerLevel;

public class ParsedFunctionCallInstruction implements PlcParsedInstruction
{
    private final String name;

    public ParsedFunctionCallInstruction(String name, NeepAsmTokenView view)
    {
        this.name = name;
    }

    @Override
    public void build(ServerLevel world, LabelLookup labelLookup, MutableProgram program) throws NeepASM.CompilationException
    {
        if (labelLookup instanceof ParsedSource parsedSource)
        {
            ParsedFunction function = parsedSource.findFunction(name);
            if (function == null)
                throw new NeepASM.CompilationException("function '" + name + "' does not exist");

            function.handle(world, parsedSource, program);
        }
    }
}
