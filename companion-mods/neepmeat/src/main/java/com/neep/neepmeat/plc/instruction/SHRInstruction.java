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

import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.compiler.parser.PlcParsedInstruction;
import com.neep.neepmeat.neepasm.vm.ThordData;
import com.neep.neepmeat.plc.Instructions;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class SHRInstruction implements PlcInstruction
{
    private final int shift;

    public SHRInstruction(int shift)
    {
        this.shift = shift;
    }

    public SHRInstruction(Supplier<Level> world, CompoundTag nbt)
    {
        this.shift = nbt.getInt("shift");
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        var stack = plc.dataStack();
        var entry = stack.pop();
        stack.push(ThordData.of(entry.intValue() >>> shift));
        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.SHR;
    }

    public static PlcParsedInstruction parser(NeepAsmTokenView view, NeepAsmParser parser, String scope) throws NeepASM.ParseException
    {
        view.fastForward();

        if (!NeepAsmTokenView.isDigit(view.peek()))
            throw new NeepASM.ParseException("expected shift integer");

        int shift = view.nextInteger();

        parser.assureLineEnd(view);

        return (world, parsedSource1, program) ->
        {
            program.addBack(new SHRInstruction(shift));
        };
    }
}
