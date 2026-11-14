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
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import com.neep.neepmeat.thord.parser.ThordDictionary;
import com.neep.neepmeat.thord.parser.ThordParsedSource;
import com.neep.neepmeat.thord.parser.ThordParser;
import com.neep.neepmeat.thord.token.ThordLexer;

import java.util.Map;

public class ThordCompilePipeline implements PLCCompilePipeline<ThordParsedSource>
{
    private final ThordParser parser;
    private final NeepAsmPLCCompiler compiler;

    private final ThordDictionary dictionary = new ThordDictionary();

    public ThordCompilePipeline(WorldSupplier worldSupplier)
    {
        Map<String, InstructionProvider> map = Instructions.REGISTRY;

        ThordLexer lexer = new ThordLexer(dictionary, map.keySet());
        this.parser = new ThordParser(lexer, map);
        this.compiler = new NeepAsmPLCCompiler(worldSupplier);
    }

    public ThordParsedSource parse(String source) throws NeepASM.ProgramBuildException
    {
        dictionary.reset();
        return parser.parse(source, dictionary);
    }

    @Override
    public PLCProgram compile(ThordParsedSource intermediate) throws NeepASM.ProgramBuildException
    {
        return compiler.compile(intermediate);
    }
}
