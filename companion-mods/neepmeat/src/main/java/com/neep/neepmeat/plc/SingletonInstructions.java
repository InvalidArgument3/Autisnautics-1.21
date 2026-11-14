/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Stack, arithmetic, and control opcodes. World and memory opcodes follow in the next batch.
 */

package com.neep.neepmeat.plc;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.api.plc.instruction.CallInstruction;
import com.neep.neepmeat.neepasm.vm.ThordData;
import com.neep.neepmeat.neepasm.vm.VM;
import com.neep.neepmeat.plc.instruction.BIFInstruction;
import com.neep.neepmeat.plc.instruction.BITInstruction;
import com.neep.neepmeat.plc.instruction.BinaryBoolInstruction;
import com.neep.neepmeat.plc.instruction.BinaryInstruction;
import com.neep.neepmeat.plc.instruction.BinaryIntInstruction;
import com.neep.neepmeat.plc.instruction.DupInstruction;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import com.neep.neepmeat.plc.instruction.OverInstruction;
import com.neep.neepmeat.plc.instruction.PCInstruction;
import com.neep.neepmeat.plc.instruction.PopInstruction;
import com.neep.neepmeat.plc.instruction.RestartInstruction;
import com.neep.neepmeat.plc.instruction.RotInstruction;
import com.neep.neepmeat.plc.instruction.SwpInstruction;
import com.neep.neepmeat.plc.instruction.UnaryInstruction;
import com.neep.neepmeat.plc.instruction.stack.FromRetInstruction;
import com.neep.neepmeat.plc.instruction.stack.PickInstruction;
import com.neep.neepmeat.plc.instruction.stack.RetAddInstruction;
import com.neep.neepmeat.plc.instruction.stack.RetEqInstruction;
import com.neep.neepmeat.plc.instruction.stack.RetFetchInstruction;
import com.neep.neepmeat.plc.instruction.stack.ToRetInstruction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class SingletonInstructions {
    public static final Instruction PC = new PCInstruction();
    public static final Instruction POP = new PopInstruction();
    public static final Instruction SWAP = new SwpInstruction();
    public static final Instruction DUP = new DupInstruction();
    public static final Instruction OVER = new OverInstruction();
    public static final Instruction ROT = new RotInstruction();
    public static final Instruction PICK = new PickInstruction();

    public static final Instruction TO_RET = new ToRetInstruction();
    public static final Instruction FROM_RET = new FromRetInstruction();
    public static final Instruction RET_FETCH = new RetFetchInstruction();
    public static final Instruction RET_ADD = new RetAddInstruction();
    public static final Instruction RET_EQ = new RetEqInstruction();
    public static final Instruction CALL = new CallInstruction((com.neep.neepmeat.neepasm.program.Label) null);

    public static final Instruction EQ = new BinaryBoolInstruction(() -> Instructions.EQ, ThordData::eq);
    public static final Instruction LT = new BinaryBoolInstruction(() -> Instructions.LT, ThordData::lt);
    public static final Instruction LTEQ = new BinaryBoolInstruction(() -> Instructions.LTEQ, ThordData::lteq);
    public static final Instruction GT = new BinaryBoolInstruction(() -> Instructions.GT, ThordData::gt);
    public static final Instruction GTEQ = new BinaryBoolInstruction(() -> Instructions.GTEQ, ThordData::gteq);

    public static final Instruction INC = new UnaryInstruction(() -> Instructions.INC, value -> value + 1);
    public static final Instruction DEC = new UnaryInstruction(() -> Instructions.DEC, value -> value - 1);
    public static final Instruction NEG = new UnaryInstruction(() -> Instructions.NEG, value -> -value);

    public static final Instruction ADD = new BinaryInstruction(() -> Instructions.ADD, ThordData::add);
    public static final Instruction SUB = new BinaryInstruction(() -> Instructions.SUB, ThordData::sub);
    public static final Instruction MUL = new BinaryInstruction(() -> Instructions.MUL, ThordData::mul);
    public static final Instruction DIV = new BinaryInstruction(() -> Instructions.DIV, ThordData::div);

    public static final Instruction NOT = new UnaryInstruction(() -> Instructions.NOT, value -> ~value);
    public static final Instruction AND = new BinaryIntInstruction(() -> Instructions.AND, (first, last) -> first & last);
    public static final Instruction OR = new BinaryIntInstruction(() -> Instructions.OR, (first, last) -> first | last);
    public static final Instruction NAND = new BinaryIntInstruction(() -> Instructions.NAND, (first, last) -> ~(first & last));
    public static final Instruction NOR = new BinaryIntInstruction(() -> Instructions.NOR, (first, last) -> ~(first | last));
    public static final Instruction XOR = new BinaryIntInstruction(() -> Instructions.XOR, (first, last) -> first ^ last);
    public static final Instruction XNOR = new BinaryIntInstruction(() -> Instructions.XNOR, (first, last) -> ~(first ^ last));

    public static final Instruction TO_INT = new ConvertInstruction(() -> Instructions.TO_INT, data -> ThordData.of(data.getAsInt()));
    public static final Instruction TO_STR = new ConvertInstruction(() -> Instructions.TO_STR, data -> ThordData.of(data.stringValue()));
    public static final Instruction RESTART = new RestartInstruction();

    private record ConvertInstruction(Supplier<InstructionProvider> opcode, Conversion function) implements Instruction {
        @Override
        public void start(VM plc) throws NeepASM.RuntimeException {
            plc.dataStack().push(function.apply(plc.dataStack().pop()));
            plc.advanceCounter();
        }

        @Override
        public @NotNull InstructionProvider getOpcode() {
            return opcode.get();
        }

        interface Conversion {
            ThordData apply(ThordData data) throws NeepASM.RuntimeException;
        }
    }
    public static final Instruction ALLOT = Instruction.EMPTY;
    public static final Instruction DYCALL = Instruction.EMPTY;
    public static final Instruction DYLOC = Instruction.EMPTY;
    public static final Instruction FETCH = Instruction.EMPTY;
    public static final Instruction FILL = Instruction.EMPTY;
    public static final Instruction STORE = Instruction.EMPTY;
}
