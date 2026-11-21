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

package com.neep.neepmeat.neepasm.compiler;

import com.neep.neepmeat.api.plc.program.MutableProgram;
import com.neep.neepmeat.api.plc.program.PLCProgram;
import com.neep.neepmeat.api.storage.WorldSupplier;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.parser.PlcParsedInstruction;
import com.neep.neepmeat.neepasm.program.Label;
import com.neep.neepmeat.plc.program.PLCProgramImpl;

/**
 * Theoretically, this should be the only part of the compilation pipeline that requires access to a World.
 */
public class NeepAsmPLCCompiler
{
    private final WorldSupplier world;

    public NeepAsmPLCCompiler(WorldSupplier world)
    {
        this.world = world;
    }

    public <T extends ParsedInstructionList & LabelLookup> PLCProgram compile(T parsedSource) throws NeepASM.ProgramBuildException
    {
        MutableProgram program = new PLCProgramImpl(world.as());

        for (var intPair : parsedSource.instructions())
        {
            try
            {
                program.setDebugLine(intPair.valueInt());

                // Certain instructions do not require a World instance
                PlcParsedInstruction.build(world.get(), intPair.key(), parsedSource, program);
            }
            catch (NeepASM.CompilationException e)
            {
                // TODO: line information in parsed source
                throw new NeepASM.ProgramBuildException(intPair.valueInt(), 0, e.getMessage());
            }
        }

        // Exported symbols
        for (Label symbol : parsedSource.symbols())
        {
            program.addSymbol(symbol);
        }

        return program;
    }
}
