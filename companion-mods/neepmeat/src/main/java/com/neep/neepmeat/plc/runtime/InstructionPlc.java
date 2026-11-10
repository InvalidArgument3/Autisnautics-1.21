/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.plc.runtime;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.api.plc.memory.MemoryManager;
import com.neep.neepmeat.api.plc.program.PLCProgram;
import com.neep.neepmeat.api.plc.robot.RobotAction;
import com.neep.neepmeat.neepasm.vm.DataStack;
import com.neep.neepmeat.neepasm.vm.ProgramResolver;
import com.neep.neepmeat.plc.arm.RoboticArmBlockEntity;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.plc.processor.PLCMemory;
import com.neep.neepmeat.plc.processor.VariableStack;
import com.neep.neepmeat.plc.robot.PLCActuator;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public final class InstructionPlc implements PLC {
    private final PLCProgram program;
    private final VariableStack data = new VariableStack(32, "data");
    private final VariableStack returns = new VariableStack(32, "return");
    private final PLCMemory memory = new PLCMemory(data);
    private final CompoundTag volatileStorage = new CompoundTag();
    private final PLCActuator actuator;
    private int counter;
    private @Nullable RobotAction action;
    private @Nullable PLCConsumer callback;
    private @Nullable String error;

    public InstructionPlc(PLCProgram program, PLCActuator actuator) {
        this.program = program;
        this.actuator = actuator;
    }

    public void tick() {
        if (actuator instanceof RoboticArmBlockEntity arm) {
            arm.serverTick();
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
                raiseError(new Error(exception));
                if (action != null) {
                    action.end(this);
                }
                action = null;
                callback = null;
            }
            return;
        }
        if (counter >= 0 && counter < program.size()) {
            try {
                program.get(counter).start(this);
            } catch (NeepASM.RuntimeException exception) {
                raiseError(new Error(exception));
            }
        }
    }

    public @Nullable String error() {
        return error;
    }

    @Override
    public void addRobotAction(RobotAction action, PLCConsumer callback) {
        this.action = action;
        this.callback = callback;
        try {
            action.start(this);
        } catch (NeepASM.RuntimeException exception) {
            raiseError(new Error(exception));
            this.action = null;
            this.callback = null;
        }
    }

    @Override
    public PLCActuator getActuator() {
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
        counter = -1;
    }

    @Override
    public int counter() {
        return counter;
    }

    @Override
    public void advanceCounter() {
        counter++;
    }

    @Override
    public void setCounter(int counter) {
        this.counter = counter;
    }

    @Override
    public ProgramResolver getProgramResolver() {
        return new ProgramResolver() {
            @Override
            public int lookupSymbol(String symbol) {
                return program.exportedSymbols().getOrDefault(symbol, -1);
            }

            @Override
            public Instruction get(int address) {
                return program.get(address);
            }

            @Override
            public void memoryAdded(com.neep.neepmeat.neepasm.vm.ProgramMemoryModule module) {
            }
        };
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
