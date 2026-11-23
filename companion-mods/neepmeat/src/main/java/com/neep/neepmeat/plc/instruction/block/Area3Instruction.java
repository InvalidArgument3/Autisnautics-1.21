package com.neep.neepmeat.plc.instruction.block;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.neepasm.vm.ThordData;
import com.neep.neepmeat.neepasm.vm.WorldTargetImpl;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import com.neep.neepmeat.plc.instruction.PlcInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

public class Area3Instruction implements PlcInstruction
{
    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        // A very cursed iteration algorithm.
        // The idea is to have 3 for loops contained in a single instruction.
        var stack = plc.dataStack();
        if (!stack.peek(0).stringValue().equals("AREA3 WORKING"))
        {
            var pos1 = stack.pop().worldTargetValue().pos();
            var pos0 = stack.pop().worldTargetValue().pos();

            int x0 = Math.min(pos0.getX(), pos1.getX());
            int x1 = Math.max(pos0.getX(), pos1.getX());
            int y0 = pos0.getY();
            int y1 = pos1.getY();
            int z0 = Math.min(pos0.getZ(), pos1.getZ());
            int z1 = Math.max(pos0.getZ(), pos1.getZ());

            stack.push(y1); // 9 to
            stack.push(y0); // 8 from
            stack.push(y0); // 7 inc
            stack.push(x1); // 6 to
            stack.push(x0); // 5 from
            stack.push(x0); // 4 inc
            stack.push(z1); // 3 to
            stack.push(z0); // 2 from
            stack.push(z0); // 1 inc
            stack.push("AREA3 WORKING");
        }

        final int yInc = 7;
        final int xInc = 4;
        final int zInc = 1;

        int y0 = stack.peekUnsafe(8).intValue();
        int prevY = stack.peekUnsafe(yInc).intValue();
        int maxY = stack.peekUnsafe(9).intValue();
        int x0 = stack.peekUnsafe(5).intValue();
        int prevX = stack.peekUnsafe(xInc).intValue();
        int maxX = stack.peekUnsafe(6).intValue();
        int z0 = stack.peekUnsafe(2).intValue();
        int prevZ = stack.peekUnsafe(zInc).intValue();
        int maxZ = stack.peekUnsafe(3).intValue();

        boolean finished = false;
        boolean place = true;
        boolean yUp = maxY > y0;
        if ((yUp && (prevY <= maxY)) || (!yUp && prevY >= maxY))
        {
            if (prevX <= maxX)
            {
                if (prevZ < maxZ)
                {
                    stack.set(zInc, ThordData.of(prevZ + 1));
                }
                else
                {
                    stack.set(zInc, ThordData.of(z0));
                    stack.set(xInc, ThordData.of(prevX + 1));
                }
            }
            else
            {
                stack.set(xInc, ThordData.of(x0));
                stack.set(yInc, ThordData.of(prevY + (yUp ? 1 : -1)));
                place = false;
            }
        }
        else
        {
            finished = true;
        }

        if (!finished)
        {
            if (place)
            {
                var addr = stack.peek(10).pointerValue();
                stack.push(new WorldTargetImpl(new BlockPos(
                        prevY % 2 == 0 ? prevX : maxX - (prevX - x0),
                        prevY,
                        prevX % 2 == 0 ? prevZ : maxZ - (prevZ - z0)
                ), Direction.UP));
                plc.returnStack().push(plc.counter());
                plc.setCounter(addr);
            }
            else
            {
                plc.setCounter(plc.counter());
            }
        }
        else
        {
            // This is horrible
            stack.pop();
            stack.pop();
            stack.pop();
            stack.pop();
            stack.pop();
            stack.pop();
            stack.pop();
            stack.pop();
            stack.pop();
            stack.pop();
            stack.pop();
            plc.advanceCounter();
        }
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.AREA3;
    }
}
