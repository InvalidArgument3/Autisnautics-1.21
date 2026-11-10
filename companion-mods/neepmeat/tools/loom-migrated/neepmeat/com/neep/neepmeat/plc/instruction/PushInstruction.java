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

package com.neep.neepmeat.plc.instruction;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.neepbus.util.message.DataVariant;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.compiler.parser.InstructionParser;
import com.neep.neepmeat.neepasm.compiler.parser.ParsedInstruction;
import com.neep.neepmeat.neepasm.vm.ThordData;
import com.neep.neepmeat.neepasm.vm.VM;
import com.neep.neepmeat.plc.Instructions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public record PushInstruction(ThordData immediate) implements Instruction
{
    public PushInstruction(int immediate)
    {
        this(ThordData.of(immediate));
    }

    public PushInstruction(Supplier<Level> ignoredWorld, CompoundTag nbt)
    {
        this(read(nbt));
//        this(DataVariant.of(nbt.getString("immediate")), nbt.getBoolean("pointer"));
    }

    private static ThordData read(CompoundTag nbt)
    {
        // Migration
        if (nbt.contains("value"))
            return ThordData.fromNbt(nbt.getCompound("value"));
        else
            return ThordData.of(DataVariant.of(nbt.getString("immediate")));
    }

    @Override
    public void start(VM plc) throws NeepASM.RuntimeException
    {
        plc.dataStack().push(immediate.copy());
        plc.advanceCounter();
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.put("value", ThordData.toNbt(immediate));
        return nbt;
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.PUSH;
    }

    @Override
    public @NotNull String toString()
    {
        return "PushInstruction{" +
                "immediate=" + immediate +
                '}';
    }

    public static class Parser implements InstructionParser
    {
        @Override
        public ParsedInstruction parse(NeepAsmTokenView view, NeepAsmParser parser, @Nullable String scope) throws NeepASM.ParseException
        {
            int val = view.nextInteger();

            parser.assureLineEnd(view);

            return (source, program) -> new PushInstruction(val);
        }
    }
}
