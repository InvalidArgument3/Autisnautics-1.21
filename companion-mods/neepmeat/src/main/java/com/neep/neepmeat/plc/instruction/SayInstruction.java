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
import com.neep.neepmeat.neepasm.compiler.parser.InstructionParser;
import com.neep.neepmeat.neepasm.compiler.parser.ParsedInstruction;
import com.neep.neepmeat.neepasm.vm.ThordData;
import com.neep.neepmeat.neepasm.vm.VM;
import com.neep.neepmeat.plc.Instructions;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SayInstruction implements Instruction
{
    private final String message;
    private final char format;

    public SayInstruction(String message)
    {
        this.message = message;
        this.format = ' ';
    }

    public SayInstruction(char format)
    {
        this.format = format;
        this.message = "";
    }

    public SayInstruction(CompoundTag nbtCompound)
    {
        this.message = nbtCompound.getString("message");
        this.format = (char) nbtCompound.getShort("format");
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.putString("message", message);
        nbt.putShort("format", (short) format);
        return nbt;
    }

    @Override
    public void start(VM plc) throws NeepASM.RuntimeException
    {
        if (message.isEmpty())
        {
            var value = plc.dataStack().pop();
            if (value.pointer() && plc instanceof PLC memory)
            {
                var data = memory.getMemory().fetch(value.intValue()).toDataVariant();
                if (format == ' ' || !data.isNumeric())
                    plc.say(data.stringValue());
                else
                    plc.say(formatNumber(data.intValue()));
            }
            else if (value instanceof ThordData.IntImpl intImpl)
            {
                plc.say(formatNumber(intImpl.value()));
            }
            else
            {
                plc.say(value.stringValue());
            }
        }
        else
        {
            plc.say(message);
        }

        plc.advanceCounter();
    }

    private String formatNumber(int value)
    {
        return format == 'b' ? Integer.toBinaryString(value) :
                format == 'h' ? Integer.toHexString(value) :
                        String.valueOf(value);
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.SAY;
    }

    public static class Parser implements InstructionParser
    {
        @Override
        public ParsedInstruction parse(NeepAsmTokenView view, NeepAsmParser parser, @Nullable String scope) throws NeepASM.ParseException
        {
            view.fastForward();

            char c = view.peek();

            if (c == '"')
            {
                String message = view.nextString();
                if (message != null)
                {
                    parser.assureLineEnd(view);
                    return (source, program) -> new SayInstruction(message);
                }
                throw new NeepASM.ParseException("expected message string");
            }

            char format;

            if (view.isIdentifier(0, c))
            {
                if (!(c == 'd' || c == 'h' || c == 'b'))
                    throw new NeepASM.ParseException("format must be 'd', 'h', or 'b'");

                format = c;
                view.next();
            }
            else
            {
                format = 'd';
            }

            parser.assureLineEnd(view);
            return (source, program) -> new SayInstruction(format);
        }
    }
}
