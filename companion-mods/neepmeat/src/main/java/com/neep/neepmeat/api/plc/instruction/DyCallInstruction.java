package com.neep.neepmeat.api.plc.instruction;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.vm.VM;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import org.jetbrains.annotations.NotNull;

public class DyCallInstruction implements Instruction
{
    @Override
    public void start(VM plc) throws NeepASM.RuntimeException
    {
        plc.returnStack().push(plc.counter() + 1);

        String name = plc.dataStack().pop().stringValue();
        int addr = plc.getProgramResolver().lookupSymbol(name);
        if (addr == -1)
            throw new NeepASM.RuntimeException("DYCALL: symbol '" + name + "' not present in context");
        plc.setCounter(addr);
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.DYCALL;
    }
}
