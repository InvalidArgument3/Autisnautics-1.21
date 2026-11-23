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

package com.neep.neepmeat.neepasm.vm;

import com.neep.meatlib.neepasm.NeepASM;

public interface VM
{
    int counter();
    void advanceCounter();
    void setCounter(int counter) throws NeepASM.RuntimeException;
    default void jumpRelative(int offset) throws NeepASM.RuntimeException
    {
        setCounter(counter() + offset);
    }
    ProgramResolver getProgramResolver();

    DataStack dataStack();
    DataStack returnStack();

//    void pushCall(int data) throws NeepASM.RuntimeException;
//    int popCall() throws NeepASM.RuntimeException;

    void say(String message);
}
