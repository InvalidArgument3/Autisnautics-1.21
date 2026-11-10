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

import com.neep.neepmeat.api.plc.instruction.InstructionBuilder;
import com.neep.neepmeat.api.plc.instruction.InstructionException;
import com.neep.neepmeat.api.plc.instruction.SimpleInstructionProvider;
import com.neep.neepmeat.api.plc.program.MutableProgram;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.parser.PlcParsedInstruction;
import com.neep.neepmeat.neepasm.program.KeyValue;
import com.neep.neepmeat.plc.instruction.Argument;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.server.level.ServerLevel;

public class DefaultParsedInstruction implements PlcParsedInstruction
{
    private final InstructionProvider provider;
    private final List<Argument> arguments;
    private final List<KeyValue> kvs;

    public DefaultParsedInstruction(InstructionProvider provider, List<Argument> arguments, List<KeyValue> kvs)
    {
        this.provider = provider;
        this.arguments = arguments;
        this.kvs = kvs;
    }

    @Override
    public void build(ServerLevel world, LabelLookup labelLookup, MutableProgram program) throws NeepASM.CompilationException
    {
        if (!(provider instanceof SimpleInstructionProvider))
            throw new NeepASM.CompilationException("default parser does not support arbitrary arguments");

        try
        {
            // InstructionBuilder emits instructions via a callback
            AtomicReference<Instruction> instruction = new AtomicReference<>();
            InstructionBuilder builder = ((SimpleInstructionProvider) provider).start(world, instruction::set);
            for (var argument : arguments)
            {
                builder = builder.argument(argument);
            }

            for (var kv : kvs)
            {
                builder = builder.keyValue(kv);
            }

            if (instruction.get() != null)
            {
                program.addBack(instruction.get());
            }
            else
            {
                throw new NeepASM.CompilationException("Insufficient arguments or key-values provided");
            }
        }
        catch (InstructionException e)
        {
            throw new NeepASM.CompilationException(e.getMessage());
        }
    }
}
