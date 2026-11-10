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

package com.neep.neepmeat.plc.editor;

import com.neep.neepmeat.api.plc.instruction.InstructionBuilder;
import com.neep.neepmeat.api.plc.instruction.InstructionException;
import com.neep.neepmeat.api.plc.instruction.SimpleInstructionProvider;
import com.neep.neepmeat.client.screen.plc.RecordMode;
import com.neep.neepmeat.network.plc.PLCErrorMessageS2C;
import com.neep.neepmeat.plc.PLCState;
import com.neep.neepmeat.plc.block.entity.PLCBlockEntity;
import com.neep.neepmeat.plc.instruction.Argument;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.plc.instruction.PlcInstruction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import org.jetbrains.annotations.Nullable;

public class ShellState implements PLCState
{
    private final PLCBlockEntity parent;

    @Nullable private InstructionBuilder instructionBuilder;
    @Nullable private InstructionProvider provider;

    public ShellState(PLCBlockEntity plc)
    {
        this.parent = plc;
    }

    @Override
    public void setInstructionBuilder(InstructionProvider provider)
    {
        if (provider instanceof SimpleInstructionProvider provider1)
        {
            instructionBuilder = provider1.start((ServerLevel) parent.getLevel(), this::emitInstruction);
        }

        this.provider = provider;
    }

    private void emitInstruction(Instruction instruction)
    {
        parent.resetError();
        parent.stop();

        if (parent.notExecuting())
        {
            parent.setCounter(0);
            parent.execute(instruction);
        }
    }

    @Override
    public void argument(Argument argument)
    {
        if (instructionBuilder != null)
        {
            try
            {
                instructionBuilder.argument(argument);
            }
            catch (InstructionException e)
            {
                PLCErrorMessageS2C.send((ServerPlayer) parent.getSurgeryRobot().getController(), e.getMessage());
            }
        }
    }

    @Override
    public RecordMode getMode()
    {
        return RecordMode.IMMEDIATE;
    }

    @Override
    public int getArgumentCount()
    {
        if (instructionBuilder == null)
            return 0;

        return instructionBuilder.argumentCount();
    }

    @Override
    public int getMaxArguments()
    {
        if (provider instanceof SimpleInstructionProvider simpleInstructionProvider)
        {
            return simpleInstructionProvider.maxArguments();
        }

        return 0;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {

    }
}
