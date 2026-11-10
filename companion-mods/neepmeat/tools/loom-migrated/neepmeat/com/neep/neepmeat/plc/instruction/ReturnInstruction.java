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

import com.neep.neepmeat.api.plc.PLC;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.plc.Instructions;
import org.jetbrains.annotations.NotNull;

public class ReturnInstruction implements PlcInstruction
{
    public static final ReturnInstruction INSTANCE = new ReturnInstruction();

    private ReturnInstruction()
    {

    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        int counter = plc.returnStack().pop().intValue();
        plc.setCounter(counter);
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.RET;
    }
}
