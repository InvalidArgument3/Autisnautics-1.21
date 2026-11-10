package com.neep.neepmeat.plc.instruction.block;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.block.entity.BuilderBlockEntity;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import com.neep.neepmeat.plc.instruction.PlcInstruction;
import com.neep.neepmeat.plc.robot.RobotMoveToAction;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class BreakBlockInstruction implements PlcInstruction
{
    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        var target = plc.dataStack().peek(0).worldTargetValue();
        if (plc.getActuator() instanceof BuilderBlockEntity)
        {
            plc.addRobotAction(new RobotMoveToAction(target.pos()), this::complete);
        }
        else
        {
            throw NeepASM.RuntimeException.notSupported(getOpcode().getParseName());
        }

    }

    private void complete(PLC plc) throws NeepASM.RuntimeException
    {
        var target = plc.dataStack().pop().worldTargetValue();
        boolean success = plc.controlActuator1(BuilderBlockEntity.class, a ->
        {
            boolean broken;
            try (Transaction transaction = Transaction.openOuter())
            {
                Consumer<ItemStack> dropped = s ->
                {
                    long inserted = a.getStorage(null).insert(ItemVariant.of(s), s.getCount(), transaction);
                    if (inserted < s.getCount())
                    {
                        Block.popResource(a.getLevel(), target.pos(), s.copyWithCount((int) (s.getCount() - inserted)));
                    }
                };

                broken = a.breakBlock(target.pos(), dropped);

                transaction.commit();
            }
            return broken;
        });
        plc.dataStack().push(success);
        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.BBREAK;
    }
}
