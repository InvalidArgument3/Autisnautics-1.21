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
import com.neep.neepmeat.neepasm.vm.VM;
import com.neep.neepmeat.plc.Instructions;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

// Branch if true
public class BITInstruction extends AbstractJumpInstruction
{
    public BITInstruction(int offset)
    {
        super(offset);
    }

    public BITInstruction(CompoundTag nbt)
    {
        super(nbt);
    }

    @Override
    public void start(VM plc) throws NeepASM.RuntimeException
    {
        if (plc.dataStack().pop().boolValue())
            plc.jumpRelative(offset);
        else
            plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.BIT;
    }

    @Override
    public String toString()
    {
        return "BIT{" + "jump=" + offset + "}";
    }
}
