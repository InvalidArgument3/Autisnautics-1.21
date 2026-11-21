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

package com.neep.neepmeat.plc.instruction;

import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.api.plc.robot.RobotAction;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.compiler.parser.PlcParsedInstruction;
import com.neep.neepmeat.plc.Instructions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class DelayInstruction implements PlcInstruction
{
    private final int time;
    private final DelayAction action = new DelayAction();

    public DelayInstruction(int time)
    {
        this.time = time;
        action.counter = time;
    }

    public DelayInstruction(Supplier<Level> world, CompoundTag nbt)
    {
        this.time = nbt.getInt("time");
        this.action.counter = nbt.getInt("counter");
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.putInt("time", time);
        nbt.putInt("counter", action.counter);
        return nbt;
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        plc.addRobotAction(action, this::finish);
    }

    private void finish(PLC plc)
    {
        action.counter = time;
        plc.advanceCounter();
    }

    @Override
    public void cancel(PLC plc)
    {
        action.counter = time;
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.DELAY;
    }

    private class DelayAction implements RobotAction
    {
        private int counter;

        @Override
        public boolean finished(PLC plc)
        {
            return counter == 0;
        }

        @Override
        public void start(PLC plc)
        {

        }

        @Override
        public void tick(PLC plc)
        {
            counter = Math.max(0, counter - 1);
        }

        @Override
        public void end(PLC plc)
        {
            counter = time;
        }
    }

    public static PlcParsedInstruction parse(NeepAsmTokenView view, NeepAsmParser parser, @Nullable String scope) throws NeepASM.ParseException
    {
        view.fastForward();
        if (!NeepAsmTokenView.isDigit(view.peek()))
            throw new NeepASM.ParseException("expected delay in ticks");

        int delay = view.nextInteger();

        return ((world, source, program) ->
                program.addBack(new DelayInstruction(delay)));
    }
}
