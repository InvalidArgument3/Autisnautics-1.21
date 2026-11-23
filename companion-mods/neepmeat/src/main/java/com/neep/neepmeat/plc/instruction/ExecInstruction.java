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

import com.google.common.collect.Lists;
import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.api.plc.program.MutableProgram;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.compiler.parser.ParsedInstruction;
import com.neep.neepmeat.neepasm.compiler.parser.PlcParsedInstruction;
import com.neep.neepmeat.neepasm.program.Label;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.block.entity.PLCExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class ExecInstruction implements PlcInstruction
{
    private final Supplier<Level> world;
    private final Argument target;
    private final Instruction instruction;

    public ExecInstruction(Supplier<Level> world, @Nullable Argument target, Instruction instruction)
    {
        this.world = world;
        this.target = target;
        this.instruction = instruction;
    }

    public ExecInstruction(Supplier<Level> world, CompoundTag nbt)
    {
        this.world = world;
        this.target = Argument.fromNbt(nbt.getCompound("target"));

        CompoundTag instructionNbt = nbt.getCompound("instruction");
        String id = instructionNbt.getString("id");
        InstructionProvider provider = Instructions.REGISTRY.get(id);
        if (provider != null)
            instruction = provider.createFromNbt(world, instructionNbt);
        else
            instruction = EMPTY;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        CompoundTag instructionNbt = new CompoundTag();
        instructionNbt.putString("id", instruction.getOpcode().getParseName());
        instruction.writeNbt(instructionNbt);

        nbt.put("instruction", instructionNbt);
        nbt.put("target", target.toNbt());
        return nbt;
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        if (world.get().getBlockEntity(target.pos()) instanceof PLCExecutor executor)
        {
            // Execute a copy of this instruction
            executor.receiveInstruction(instruction.getOpcode().createFromNbt(world, instruction.writeNbt(new CompoundTag())));
        }
        else
        {
            throw new NeepASM.RuntimeException("Exec not supported by target");
        }

        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.EXEC;
    }

    public static PlcParsedInstruction parser(NeepAsmTokenView view, NeepAsmParser parser, String scope) throws NeepASM.ParseException
    {
        view.fastForward();
        Argument target = parser.parseWorldTarget(view);
        if (target == null)
            throw new NeepASM.ParseException("expected argument after exec");

        ParsedInstruction parsedInstruction = parser.parseInstruction(view, null);
        if (parsedInstruction == null)
            throw new NeepASM.ParseException("expected instruction after exec");

        return (world, source, program) ->
        {
            DummyProgram dummyProgram = new DummyProgram();

            PlcParsedInstruction.build(world, parsedInstruction, source, dummyProgram);
            Instruction instruction = dummyProgram.get(0);

            if (instruction == EMPTY)
                throw new NeepASM.CompilationException("exec instruction is empty");

            if (instruction.getOpcode().equals(Instructions.EXEC))
                throw new NeepASM.CompilationException("nested exec instructions not allowed");

            program.addBack(new ExecInstruction(() -> world, target, instruction));
        };
    }

    private static class DummyProgram implements MutableProgram
    {
        public List<Instruction> instructions = Lists.newArrayList();

        @Override
        public CompoundTag writeNbt(CompoundTag nbt)
        {
            return nbt;
        }

        @Override
        public void readNbt(CompoundTag nbt)
        {

        }

        @Override
        public void addBack(Instruction instruction)
        {
            instructions.add(instruction);
        }

        @Override
        public void setDebugLine(int line) { }

        @Override
        public void addSymbol(Label symbol) { }

        @Override
        public Instruction get(int offset)
        {
            if (offset < instructions.size())
            {
                return instructions.get(offset);
            }
            return Instruction.end();
        }

        @Override
        public int size()
        {
            return instructions.size();
        }

        @Override
        public int getDebugLine(int counter)
        {
            return -1;
        }

        @Override
        public Map<String, Integer> exportedSymbols()
        {
            return Map.of();
        }
    }
}
