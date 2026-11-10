package com.neep.neepmeat.plc.instruction.block;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.block.entity.BuilderBlockEntity;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import com.neep.neepmeat.plc.instruction.PlcInstruction;
import org.jetbrains.annotations.NotNull;

public class SelSlotInstruction implements PlcInstruction
{
    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        plc.controlActuator(BuilderBlockEntity.class, b ->
        {
            b.setSlot(plc.dataStack().pop().intValue());
        });
        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.SELSLOT;
    }
}
