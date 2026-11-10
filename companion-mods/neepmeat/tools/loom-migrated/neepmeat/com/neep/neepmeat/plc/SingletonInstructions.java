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

package com.neep.neepmeat.plc;

import com.neep.neepmeat.api.plc.instruction.CallInstruction;
import com.neep.neepmeat.api.plc.instruction.DyCallInstruction;
import com.neep.neepmeat.api.plc.instruction.DyLocInstruction;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.program.Label;
import com.neep.neepmeat.neepasm.vm.ThordData;
import com.neep.neepmeat.neepasm.vm.VM;
import com.neep.neepmeat.plc.instruction.*;
import com.neep.neepmeat.plc.instruction.block.*;
import com.neep.neepmeat.plc.instruction.memory.AllotInstruction;
import com.neep.neepmeat.plc.instruction.memory.FetchInstruction;
import com.neep.neepmeat.plc.instruction.memory.FillInstruction;
import com.neep.neepmeat.plc.instruction.memory.StoreInstruction;
import com.neep.neepmeat.plc.instruction.stack.*;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class SingletonInstructions
{
    public static final Instruction PC = new PCInstruction();

    public static final Instruction POP = new PopInstruction();
    public static final Instruction SWAP = new SwpInstruction();
    public static final Instruction DUP = new DupInstruction();
    public static final Instruction OVER = new OverInstruction();
    public static final Instruction ROT = new RotInstruction();
    public static final Instruction PICK = new PickInstruction();

    public static final Instruction DYCALL = new DyCallInstruction();
    public static final Instruction DYLOC = new DyLocInstruction();

    public static final Instruction TO_RET = new ToRetInstruction();
    public static final Instruction FROM_RET = new FromRetInstruction();
    public static final Instruction RET_FETCH = new RetFetchInstruction();
    public static final Instruction RET_ADD = new RetAddInstruction();
    public static final Instruction RET_EQ = new RetEqInstruction();
    public static final Instruction CALL = new CallInstruction((Label) null);

    public static final PlcInstruction FREE = new FreeInstruction();
    public static final PlcInstruction STORE = new StoreInstruction();
    public static final PlcInstruction ALLOT = new AllotInstruction();
    public static final PlcInstruction FETCH = new FetchInstruction();
    public static final PlcInstruction FILL= new FillInstruction();

    public static final Instruction EQ = new BinaryBoolInstruction(() -> Instructions.EQ, ThordData::eq);
    public static final Instruction LT = new BinaryBoolInstruction(() -> Instructions.LT, ThordData::lt);
    public static final Instruction LTEQ = new BinaryBoolInstruction(() -> Instructions.LTEQ, ThordData::lteq);
    public static final Instruction GT = new BinaryBoolInstruction(() -> Instructions.GT, ThordData::gt);
    public static final Instruction GTEQ = new BinaryBoolInstruction(() -> Instructions.GTEQ, ThordData::gteq);

    public static final Instruction INC = new UnaryInstruction(() -> Instructions.INC, l -> l + 1);
    public static final Instruction DEC = new UnaryInstruction(() -> Instructions.DEC, l -> l - 1);
    public static final Instruction NEG = new UnaryInstruction(() -> Instructions.NEG, l -> -l);

    public static final Instruction ADD = new BinaryInstruction(() -> Instructions.ADD, ThordData::add);
    public static final Instruction SUB = new BinaryInstruction(() -> Instructions.SUB, ThordData::sub);
    public static final Instruction MUL = new BinaryInstruction(() -> Instructions.MUL, ThordData::mul);
    public static final Instruction DIV = new BinaryInstruction(() -> Instructions.DIV, ThordData::div);

    public static final Instruction NOT = new UnaryInstruction(() -> Instructions.NOT, (l) -> ~l);
    public static final Instruction AND = new BinaryIntInstruction(() -> Instructions.AND, (f, l) -> f & l);
    public static final Instruction OR = new BinaryIntInstruction(() -> Instructions.OR, (f, l) -> f | l);
    public static final Instruction NAND = new BinaryIntInstruction(() -> Instructions.NAND, (f, l) -> ~(f & l));
    public static final Instruction NOR = new BinaryIntInstruction(() -> Instructions.NOR, (f, l) -> ~(f | l));
    public static final Instruction XOR = new BinaryIntInstruction(() -> Instructions.XOR, (f, l) -> f ^ l);
    public static final Instruction XNOR = new BinaryIntInstruction(() -> Instructions.XNOR, (f, l) -> ~(f ^ l));

    public static final Instruction TO_INT = new ConvertInstruction(() -> Instructions.TO_INT, x -> ThordData.of(x.intValue()));
    public static final Instruction TO_STR = new ConvertInstruction(() -> Instructions.TO_STR, x -> ThordData.of(x.stringValue()));
    public static final Instruction RESTART = new RestartInstruction();

    public static final Instruction BPLACE = new PlaceBlockInstruction();
    public static final Instruction BBREAK = new BreakBlockInstruction();
    public static final Instruction MOVETO = new MoveToInstruction();
    public static final Instruction SELSLOT = new SelSlotInstruction();
    public static final Instruction AREA3 = new Area3Instruction();

    private record ConvertInstruction(Supplier<InstructionProvider> opcode, Conversion function) implements Instruction
    {
        @Override
        public void start(VM plc) throws NeepASM.RuntimeException
        {
            plc.dataStack().push(function.apply(plc.dataStack().pop()));
            plc.advanceCounter();
        }

        @Override
        public @NotNull InstructionProvider getOpcode()
        {
            return opcode.get();
        }

        interface Conversion
        {
            ThordData apply(ThordData data) throws NeepASM.RuntimeException;
        }
    }
}
