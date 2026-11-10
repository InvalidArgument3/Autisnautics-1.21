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

import com.neep.neepmeat.api.plc.program.MutableProgram;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.LabelLookup;
import com.neep.neepmeat.plc.instruction.Instruction;
import net.minecraft.server.level.ServerLevel;

public interface PlcParsedInstruction extends ParsedInstruction
{
    static ParsedInstruction of(PlcParsedInstruction parsed)
    {
        return parsed;
    }

    default Instruction build(LabelLookup labelLookup, MutableProgram program) throws NeepASM.CompilationException
    {
        throw new NeepASM.CompilationException("world access required");
    }

    // TODO: Return instruction directly instead of relying on implementation to emit it
    void build(ServerLevel world, LabelLookup labelLookup, MutableProgram program) throws NeepASM.CompilationException;

    @Override
    default boolean supportsCompiler()
    {
        return false;
    }

    static void build(ServerLevel serverWorld, ParsedInstruction parsedInstruction, LabelLookup lookup, MutableProgram program) throws NeepASM.CompilationException
    {
        if (parsedInstruction instanceof PlcParsedInstruction plcParsed)
        {
            plcParsed.build(serverWorld, lookup, program);
        }
        else
        {
            Instruction instruction = parsedInstruction.build(lookup, program);
            program.addBack(instruction);
        }
    }
}
