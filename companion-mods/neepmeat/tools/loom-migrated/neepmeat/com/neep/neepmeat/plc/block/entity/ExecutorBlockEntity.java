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

package com.neep.neepmeat.plc.block.entity;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.api.plc.memory.MemoryManager;
import com.neep.neepmeat.api.plc.robot.RobotAction;
import com.neep.neepmeat.api.storage.DynamicLazyBlockApiCache;
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.vm.DataStack;
import com.neep.neepmeat.neepasm.vm.ProgramResolver;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.block.ExecutorBlock;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import com.neep.neepmeat.plc.instruction.PlcInstruction;
import com.neep.neepmeat.plc.processor.VariableStack;
import com.neep.neepmeat.plc.robot.PLCActuator;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Collection;

public class ExecutorBlockEntity extends SyncableBlockEntity implements PLC, PLCExecutor
{
    private final LazyBlockApiCache<Void, Void> cache = addListener(DynamicLazyBlockApiCache.voidCtx(MeatLib.VOID_LOOKUP, this::getLevel,
            () -> getBlockPos().relative(getBlockState().getValue(ExecutorBlock.FACING))));

    @Nullable private Pair<RobotAction, PLCConsumer> currentAction;

    private final ArrayDeque<Instruction> instructions = new ArrayDeque<>();
    private PlcInstruction currentInstruction;

    private int flag;
    private final VariableStack variableStack = new VariableStack(8, "data stack"); // TODO: save
    @Nullable private Error error;
    private CompoundTag volatileStorage = new CompoundTag();

    public ExecutorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public void serverTick()
    {
        if (error != null)
        {
            stop();
            say(error.what());
            error = null;
        }

        if (currentInstruction == null && !instructions.isEmpty())
        {
            execute(instructions.peek());
        }

        if (currentAction != null)
        {
            if (currentAction.first().finished(this))
            {
                currentAction.first().end(this);
                try
                {
                    currentAction.second().accept(this);
                }
                catch (NeepASM.RuntimeException e)
                {
                    raiseError(e);
                }
                currentAction = null;
                setChanged();
            }
            else
            {
                try
                {
                    currentAction.first().tick(this);
                }
                catch (NeepASM.RuntimeException e)
                {
                    raiseError(e);
                }
            }
        }
        updateState();
    }

    private void updateState()
    {
        BlockState state = getBlockState();
        boolean prev = state.getValue(ExecutorBlock.ON);
        if ((!instructions.isEmpty() || currentInstruction != null) != prev)
        {
            level.setBlockAndUpdate(worldPosition, state.setValue(ExecutorBlock.ON, !instructions.isEmpty()));
        }
    }

    public void execute(Instruction instruction)
    {
        if (instruction instanceof PlcInstruction plcInstruction)
        {
            if (currentAction != null)
            {
                currentAction = null;
            }

            currentInstruction = plcInstruction;

            try
            {
                if (plcInstruction.canStart(this))
                {
                    plcInstruction.start(this);
                    if (currentAction != null)
                    {
                        currentAction.first().start(this);
                    }
                }
            }
            catch (NeepASM.RuntimeException e)
            {
                raiseError(e);
            }

            setChanged();
        }
        else
        {
            raiseError(new Error("Executor does not support " + instruction.getOpcode().getShortName()));
        }
    }

    private void say(Component what)
    {
        PlayerLookup.around((ServerLevel) getLevel(), getBlockPos(), 20).forEach(p -> p.sendSystemMessage(
                Component.nullToEmpty("[Executor at " + getBlockPos().getX() + " " + getBlockPos().getY() + " " + getBlockPos().getZ() + "] ").copy().append(what)));
    }

    @Override
    public PLCActuator getActuator()
    {
        if (cache.getBlockEntity() instanceof PLCActuator actuator)
            return actuator;

        return PLCActuator.EMPTY;
    }

    @Override
    public CompoundTag volatileStorage()
    {
        return volatileStorage;
    }

    @Override
    public void selectActuator(@Nullable BlockPos pos)
    {

    }

    @Override
    public void addRobotAction(RobotAction action, PLCConsumer callback)
    {
        currentAction = Pair.of(action, callback);
    }

    @Override
    public int counter()
    {
        return 0;
    }

    @Override
    public void advanceCounter()
    {
        currentInstruction = null;
        instructions.poll();
        setChanged();
    }

    public void stop()
    {
        if (currentInstruction != null)
            currentInstruction.cancel(this);

        currentInstruction = null;
        instructions.clear();

        currentAction = null;

        setChanged();
    }

    @Override
    public VariableStack dataStack()
    {
        return variableStack;
    }

    @Override
    public DataStack returnStack()
    {
        return variableStack;
    }

    @Override
    public void say(String message)
    {
        PlayerLookup.around((ServerLevel) getLevel(), worldPosition, 20).forEach(p -> p.sendSystemMessage(
                Component.nullToEmpty("[Executor at " + worldPosition.getX() + " " + worldPosition.getY() + " " + worldPosition.getZ() + "] " + message)));
    }

    @Override
    public MemoryManager getMemory()
    {
        return MemoryManager.EMPTY;
    }

    @Override
    public void setCounter(int counter)
    {
        if (counter == -1)
            stop();
    }

    @Override
    public ProgramResolver getProgramResolver()
    {
        return ProgramResolver.EMPTY;
    }

    @Override
    public void raiseError(Error error)
    {
        this.error = error;

        if (currentInstruction != null)
        {
            currentInstruction.cancel(this);
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putInt("flag", flag);

        ListTag list = new ListTag();
        for (Instruction instruction : instructions)
        {
            CompoundTag instructionNbt = new CompoundTag();
            instruction.writeNbt(instructionNbt);
            instructionNbt.putString("id", instruction.getOpcode().getParseName());
            list.add(instructionNbt);
        }
        nbt.put("instructions", list);

//        nbt.put("variable_stack", variableStack.writeNbt(new NbtCompound()));
        nbt.put("volatile_storage", volatileStorage.copy());
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.flag = nbt.getInt("flag");

        instructions.clear();
        ListTag list = nbt.getList("instructions", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i)
        {
            CompoundTag instructionNbt = list.getCompound(i);

            String id = instructionNbt.getString("id");
            InstructionProvider provider = Instructions.REGISTRY.get(id);
            if (provider != null)
            {
                instructions.add(provider.createFromNbt(this::getLevel, instructionNbt));
            }
        }

//        variableStack.readNbt(nbt.getCompound("variable_stack"));
        volatileStorage = nbt.getCompound("volatile_storage");
    }

    public void raiseError(NeepASM.RuntimeException e)
    {
        raiseError(new Error(e));
    }

    @Override
    public void receiveInstruction(Instruction instruction) throws NeepASM.RuntimeException
    {
        if (instructions.size() < 16)
            instructions.add(instruction);
        else
            throw new NeepASM.RuntimeException("executor [%s %s %s] reached maximum number of queued instructions (16)".formatted(
                    worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()
            ));
    }

    public Collection<Instruction> getInstructions()
    {
        return instructions;
    }
}
