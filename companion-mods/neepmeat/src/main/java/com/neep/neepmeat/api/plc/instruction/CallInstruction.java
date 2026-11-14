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

package com.neep.neepmeat.api.plc.instruction;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.program.Label;
import com.neep.neepmeat.neepasm.vm.VM;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CallInstruction implements Instruction
{
    @Nullable private final Label label;

    public CallInstruction(CompoundTag nbt)
    {
        if (nbt.contains("target"))
        {
            this.label = new Label(nbt.getString("name"), nbt.getInt("target"));
        }
        else
        {
            this.label = null;
        }
    }

    public CallInstruction(@Nullable Label label)
    {
        this.label = label;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        if (label != null)
        {
            nbt.putString("name", label.name());
            nbt.putInt("target", label.index());
        }
        return nbt;
    }

    @Override
    public void start(VM plc) throws NeepASM.RuntimeException
    {
        // Push the index of the next instruction, not this current one.
        plc.returnStack().push(plc.counter() + 1);
        if (label != null)
        {
//            plc.setCounter(label.index());
            int addr = plc.getProgramResolver().lookupSymbol(label.name());
            plc.setCounter(addr);
        }
        else
        {
            plc.setCounter(plc.dataStack().pop().pointerValue());
        }
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.CALL;
    }
}
