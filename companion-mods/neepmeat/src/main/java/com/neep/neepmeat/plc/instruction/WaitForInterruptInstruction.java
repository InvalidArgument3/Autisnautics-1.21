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

import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.api.plc.interrupt.InterruptReceiver;
import com.neep.neepmeat.api.plc.robot.RobotAction;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.plc.Instructions;
import org.jetbrains.annotations.NotNull;

public class WaitForInterruptInstruction implements PlcInstruction
{
    public static final WaitForInterruptInstruction INSTANCE = new WaitForInterruptInstruction();

    private WaitForInterruptInstruction()
    {
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        if (plc instanceof InterruptReceiver receiver)
        {
            receiver.getInterrupts().setEnabled(true);
        }

        plc.addRobotAction(new WaitAction(), this::finish);
    }

    private void finish(PLC plc)
    {
        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.IWAIT;
    }

    private static class WaitAction implements RobotAction
    {

        @Override
        public boolean finished(PLC plc)
        {
            if (plc instanceof InterruptReceiver receiver)
            {
                return receiver.getInterrupts().hasQueued();
            }

            return false;
        }
        @Override
        public void start(PLC plc)
        {

        }

        @Override
        public void tick(PLC plc)
        {

        }

        @Override
        public void end(PLC plc)
        {

        }

    }
    public static void disableInterrupts(PLC plc)
    {
        if (plc instanceof InterruptReceiver receiver)
        {
            receiver.setEnabled(false);
        }
        plc.advanceCounter();
    }

    public static void enableInterrupts(PLC plc)
    {
        if (plc instanceof InterruptReceiver receiver)
        {
            receiver.setEnabled(true);
        }
        plc.advanceCounter();
    }

    public static void clearInterrupts(PLC plc)
    {
        if (plc instanceof InterruptReceiver receiver)
        {
            receiver.getInterrupts().clear();
        }
        plc.advanceCounter();
    }
}
