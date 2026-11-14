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
import com.neep.neepmeat.neepasm.vm.ThordData;
import com.neep.neepmeat.neepasm.vm.VM;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class BinaryInstruction implements Instruction
{
    private final Supplier<InstructionProvider> provider;
    private final BinaryOperation operation;

    public BinaryInstruction(Supplier<InstructionProvider> provider, BinaryOperation operation)
    {
        this.provider = provider;
        this.operation = operation;
    }

    @Override
    public void start(VM plc) throws NeepASM.RuntimeException
    {
        var last = plc.dataStack().pop();
        var first = plc.dataStack().pop();
        plc.dataStack().push(operation.apply(first, last));
        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return provider.get();
    }
    
    @FunctionalInterface
    public interface BinaryOperation
    {
        ThordData apply(ThordData first, ThordData last) throws NeepASM.RuntimeException;
    }
}
