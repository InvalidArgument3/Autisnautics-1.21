/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.plc.block.entity;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.api.plc.memory.MemoryManager;
import com.neep.neepmeat.api.plc.robot.RobotAction;
import com.neep.neepmeat.machine.NeepMachines;
import com.neep.neepmeat.neepasm.vm.DataStack;
import com.neep.neepmeat.neepasm.vm.ProgramResolver;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.plc.processor.PLCMemory;
import com.neep.neepmeat.plc.processor.VariableStack;
import com.neep.neepmeat.plc.robot.PLCActuator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;

public class ExecutorBlockEntity extends BlockEntity implements PLC, PLCExecutor {
    private final ArrayDeque<Instruction> instructions = new ArrayDeque<>();
    private final VariableStack data = new VariableStack(32, "data");
    private final VariableStack returns = new VariableStack(32, "return");
    private final PLCMemory memory = new PLCMemory(data);
    private final CompoundTag volatileStorage = new CompoundTag();
    private @Nullable RobotAction action;
    private @Nullable PLCConsumer callback;
    private @Nullable String error;

    public ExecutorBlockEntity(BlockPos pos, BlockState state) {
        super(NeepMachines.EXECUTOR_BE.get(), pos, state);
    }

    @Override
    public void receiveInstruction(Instruction instruction) throws NeepASM.RuntimeException {
        instructions.add(instruction);
        setChanged();
    }

    public void serverTick() {
        PLCActuator actuator = findActuator();
        if (actuator instanceof BuilderBlockEntity builder) {
            builder.serverTick();
        }
        if (action != null) {
            try {
                action.tick(this);
                if (action.finished(this)) {
                    action.end(this);
                    PLCConsumer done = callback;
                    action = null;
                    callback = null;
                    if (done != null) {
                        done.accept(this);
                    }
                }
            } catch (NeepASM.RuntimeException exception) {
                error = exception.getMessage();
                action = null;
            }
            return;
        }
        Instruction next = instructions.poll();
        if (next == null) {
            return;
        }
        try {
            next.start(this);
        } catch (NeepASM.RuntimeException exception) {
            error = exception.getMessage();
        }
        setChanged();
    }

    public @Nullable String error() {
        return error;
    }

    public DataStack stack() {
        return data;
    }

    public @Nullable PLCActuator findActuator() {
        if (level == null) {
            return null;
        }
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockEntity entity = level.getBlockEntity(worldPosition.relative(facing));
        if (entity instanceof PLCActuator.Provider provider) {
            return provider.getPlcActuator();
        }
        return entity instanceof PLCActuator actuator ? actuator : null;
    }

    @Override
    public void addRobotAction(RobotAction action, PLCConsumer callback) {
        this.action = action;
        this.callback = callback;
        try {
            action.start(this);
        } catch (NeepASM.RuntimeException exception) {
            error = exception.getMessage();
            this.action = null;
        }
    }

    @Override
    public PLCActuator getActuator() {
        PLCActuator actuator = findActuator();
        if (actuator == null) {
            throw new IllegalStateException("Executor has no actuator");
        }
        return actuator;
    }

    @Override
    public CompoundTag volatileStorage() {
        return volatileStorage;
    }

    @Override
    public void selectActuator(@Nullable BlockPos pos) {
    }

    @Override
    public MemoryManager getMemory() {
        return memory;
    }

    @Override
    public void raiseError(Error raised) {
        error = raised.what().getString();
    }

    @Override
    public int counter() {
        return 0;
    }

    @Override
    public void advanceCounter() {
    }

    @Override
    public void setCounter(int counter) {
    }

    @Override
    public ProgramResolver getProgramResolver() {
        return ProgramResolver.EMPTY;
    }

    @Override
    public DataStack dataStack() {
        return data;
    }

    @Override
    public DataStack returnStack() {
        return returns;
    }
}
