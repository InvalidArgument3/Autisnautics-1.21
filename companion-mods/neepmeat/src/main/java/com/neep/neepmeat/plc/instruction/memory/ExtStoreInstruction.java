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

package com.neep.neepmeat.plc.instruction.memory;

import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.api.plc.memory.MemoryProvider;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.compiler.parser.PlcParsedInstruction;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.instruction.Argument;
import com.neep.neepmeat.plc.instruction.PlcInstruction;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class ExtStoreInstruction implements PlcInstruction
{
    private final Supplier<Level> world;
    private final Argument argument;

    public ExtStoreInstruction(Supplier<Level> world, CompoundTag nbt)
    {
        this(world, Argument.fromNbt(nbt.getCompound("target")));
    }

    public ExtStoreInstruction(Supplier<Level> world, Argument argument)
    {
        this.world = world;
        this.argument = argument;
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        var stack = plc.dataStack();
        if (world.get().getBlockEntity(argument.pos()) instanceof MemoryProvider memoryProvider)
        {
            var entry = plc.dataStack().pop();
            if (entry.pointer())
            {
                var memEntry = plc.getMemory().fetch(entry.pointerValue());
                memoryProvider.push(memEntry.toMemoryEntry());
            }
            else
            {
                memoryProvider.push(stack.pop().toMemoryEntry());
            }
        }
        else
        {
            throw new NeepASM.RuntimeException("EXTSTORE: Block provides no memory");
        }
        plc.advanceCounter();
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.put("target", argument.toNbt());
        return nbt;
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.EXTSTORE;
    }

    public static PlcParsedInstruction parser(NeepAsmTokenView view, NeepAsmParser parser, String scope) throws NeepASM.ParseException
    {
        view.fastForward();

        Argument argument = parser.parseWorldTarget(view);
        if (argument == null)
            throw new NeepASM.ParseException("Expected world target to store memory in");

        parser.assureLineEnd(view);

        return (world, labelLookup, program) -> program.addBack(new ExtStoreInstruction(() -> world, argument));
    }
}
