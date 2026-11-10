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

package com.neep.neepmeat.thord.compiler;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.vm.DataStack;
import com.neep.neepmeat.neepasm.vm.ProgramResolver;
import com.neep.neepmeat.neepasm.vm.VM;
import com.neep.neepmeat.plc.processor.VariableStack;

public class CompilerVM implements VM
{
    private final DataStack dataStack;
    private final DataStack returnStack;

    private int counter;

    public CompilerVM(DataStack dataStack)
    {
        this.dataStack = dataStack;
        this.returnStack = new VariableStack(32, "compiler return stack");
    }

    public void push(int data) throws NeepASM.RuntimeException
    {
        dataStack.push(data);
    }

    public int popInt() throws NeepASM.RuntimeException
    {
        return dataStack.pop().intValue();
    }

    @Override
    public int counter()
    {
        return counter;
    }

    @Override
    public void advanceCounter()
    {
        counter++;
    }

    @Override
    public void setCounter(int counter) throws NeepASM.RuntimeException
    {
        if (counter > this.counter)
        {
            this.counter = counter;
        }
        else
        {
            throw new NeepASM.RuntimeException("cannot branch backwards in compiler");
        }
    }

    @Override
    public ProgramResolver getProgramResolver()
    {
        return ProgramResolver.EMPTY;
    }

    @Override
    public DataStack dataStack()
    {
        return dataStack;
    }

    @Override
    public DataStack returnStack()
    {
        return returnStack;
    }

    @Override
    public void say(String message)
    {
//        System.out.println(message);
    }

    public void resetCounter()
    {
        counter = 0;
    }
}
