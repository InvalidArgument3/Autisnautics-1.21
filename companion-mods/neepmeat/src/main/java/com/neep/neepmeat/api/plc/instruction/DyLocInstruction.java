package com.neep.neepmeat.api.plc.instruction;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.vm.VM;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import org.jetbrains.annotations.NotNull;

public class DyLocInstruction implements Instruction
{
    @Override
    public void start(VM plc) throws NeepASM.RuntimeException
    {
        String name = plc.dataStack().pop().stringValue();
        int addr = plc.getProgramResolver().lookupSymbol(name);
        plc.dataStack().push(addr);
        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.DYLOC;
    }
}
