package com.neep.neepmeat.neepasm.vm;

import com.neep.neepmeat.plc.instruction.Instruction;

public interface ProgramResolver
{
    int lookupSymbol(String symbol);
    Instruction get(int address);
    void memoryAdded(ProgramMemoryModule module);

    ProgramResolver EMPTY = new ProgramResolver()
    {
        @Override
        public int lookupSymbol(String symbol)
        {
            return -1;
        }

        @Override
        public Instruction get(int address)
        {
            return Instruction.EMPTY;
        }

        @Override
        public void memoryAdded(ProgramMemoryModule module) { }
    };
}
