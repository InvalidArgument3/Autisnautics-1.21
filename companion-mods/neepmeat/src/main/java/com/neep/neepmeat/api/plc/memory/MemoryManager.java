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

package com.neep.neepmeat.api.plc.memory;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.vm.ThordData;

public interface MemoryManager
{
    // #include stddef.h
    int NULL = 0;

    void clear();
    void store(int address, ThordData data) throws NeepASM.RuntimeException;
    ThordData fetch(int pointer);
    ThordData allocate(ThordData entry) throws NeepASM.RuntimeException;
    int allocateSize(int size) throws NeepASM.RuntimeException;

    void free(int pointer) throws NeepASM.RuntimeException;

    MemoryManager EMPTY = new MemoryManager()
    {
        @Override
        public void clear()
        {

        }

        @Override
        public void store(int address, ThordData data) throws NeepASM.RuntimeException
        {

        }

        @Override
        public ThordData allocate(ThordData entry)
        {
            return new ThordData.PointerImpl(NULL);
        }

        @Override
        public int allocateSize(int size)
        {
            return NULL;
        }

        @Override
        public void free(int pointer)
        {

        }

        @Override
        public ThordData fetch(int pointer)
        {
            return ThordData.NULL;
        }
    };
}
