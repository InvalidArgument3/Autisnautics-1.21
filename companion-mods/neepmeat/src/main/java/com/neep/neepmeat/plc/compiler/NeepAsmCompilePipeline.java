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

package com.neep.neepmeat.plc.compiler;

import com.neep.neepmeat.api.plc.program.PLCProgram;
import com.neep.neepmeat.api.storage.WorldSupplier;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.NeepAsmPLCCompiler;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.compiler.ParsedSource;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.instruction.InstructionProvider;

import java.util.Map;

public class NeepAsmCompilePipeline implements PLCCompilePipeline<ParsedSource>
{
    private final NeepAsmParser parser;
    private final NeepAsmPLCCompiler compiler;

    public NeepAsmCompilePipeline(WorldSupplier worldSupplier)
    {
        Map<String, InstructionProvider> map = Instructions.REGISTRY;

        compiler = new NeepAsmPLCCompiler(worldSupplier);
        parser = new NeepAsmParser(map);
    }

    @Override
    public ParsedSource parse(String source) throws NeepASM.ProgramBuildException
    {
        return parser.parse(source);
    }

    @Override
    public PLCProgram compile(ParsedSource source) throws NeepASM.ProgramBuildException
    {
        return compiler.compile(source);
    }
}
