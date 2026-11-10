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

public interface DataStack
{
    void push(ThordData data) throws NeepASM.RuntimeException;
    default void push(boolean value) throws NeepASM.RuntimeException { push(ThordData.of(value)); }
    default void push(int value) throws NeepASM.RuntimeException { push(ThordData.of(value)); }
    default void push(String value) throws NeepASM.RuntimeException { push(ThordData.of(value)); }

    ThordData pop() throws NeepASM.RuntimeException;
//    int popInt() throws NeepASM.RuntimeException;

//    int peekInt(int i) throws NeepASM.RuntimeException;
    /// Result is copied for safety
    default ThordData last(int i) throws NeepASM.RuntimeException { return peekUnsafe(i).copy(); }

    /// Evil method to be used infrequently
    void set(int i, ThordData of) throws NeepASM.RuntimeException;

    /// Result may have remaining references. Do not mutate.
    ThordData peekUnsafe(int i) throws NeepASM.RuntimeException;

    default ThordData peek(int i) throws NeepASM.RuntimeException {
        return peekUnsafe(i);
    }

    int size();
    boolean isEmpty();
}
