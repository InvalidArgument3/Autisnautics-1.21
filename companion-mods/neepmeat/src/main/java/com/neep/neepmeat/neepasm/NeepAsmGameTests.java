/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.neepasm;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.plc.program.PLCProgram;
import com.neep.neepmeat.neepasm.vm.DataStack;
import com.neep.neepmeat.neepasm.vm.ProgramResolver;
import com.neep.neepmeat.neepasm.vm.VM;
import com.neep.neepmeat.plc.compiler.NeepAsmCompilePipeline;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.plc.processor.VariableStack;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(NeepMeat.ID)
@PrefixGameTestTemplate(false)
public class NeepAsmGameTests {
    @GameTest(template = "empty")
    public static void compilesAndAddsTwoAndThree(GameTestHelper helper) {
        try {
            ServerLevel level = helper.getLevel();
            NeepAsmCompilePipeline pipeline = new NeepAsmCompilePipeline(() -> level);
            PLCProgram program = pipeline.compile(pipeline.parse("""
                    push 2
                    push 3
                    add
                    """));
            DirectVm vm = new DirectVm(program);
            int steps = 0;
            while (vm.counter() >= 0 && vm.counter() < program.size() && steps++ < 16) {
                program.get(vm.counter()).start(vm);
            }
            helper.assertTrue(vm.dataStack().pop().getAsInt() == 5, "2 + 3");
            helper.succeed();
        } catch (NeepASM.ProgramBuildException | NeepASM.RuntimeException exception) {
            helper.fail(exception.getMessage());
        }
    }

    private static final class DirectVm implements VM {
        private final PLCProgram program;
        private final VariableStack data = new VariableStack(32, "data");
        private final VariableStack returns = new VariableStack(32, "return");
        private int counter;

        private DirectVm(PLCProgram program) {
            this.program = program;
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

        @Override
        public void say(String message) {
        }
    }
}
