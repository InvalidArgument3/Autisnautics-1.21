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

public interface Instruction
{
    Instruction EMPTY = new Instruction()
    {
        @Override
        public void start(VM plc) throws NeepASM.RuntimeException
        {
            plc.setCounter(-1);
        }

        @Override
        public @NotNull InstructionProvider getOpcode()
        {
            return Instructions.END;
        }

        @Override
        public String toString()
        {
            return "Empty Instruction";
        }
    };

    static Instruction end() { return EMPTY; }

    void start(VM plc) throws NeepASM.RuntimeException;

    @NotNull InstructionProvider getOpcode();

    default CompoundTag writeNbt(CompoundTag nbt) { return nbt; };
}
