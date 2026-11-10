/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Stack, arithmetic, and jump opcodes. Item, fluid, redstone, and block opcodes
 * are registered when those instruction classes are ported.
 */

package com.neep.neepmeat.plc;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.neepmeat.api.plc.instruction.CallInstruction;
import com.neep.neepmeat.api.plc.instruction.SimplerInstructionProvider;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.compiler.parser.CallInstructionParser;
import com.neep.neepmeat.neepasm.compiler.parser.InstructionParser;
import com.neep.neepmeat.neepasm.compiler.parser.JumpInstructionParser;
import com.neep.neepmeat.neepasm.compiler.parser.ParsedInstruction;
import com.neep.neepmeat.neepasm.program.Label;
import com.neep.neepmeat.plc.instruction.BIFInstruction;
import com.neep.neepmeat.plc.instruction.BITInstruction;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import com.neep.neepmeat.plc.instruction.JumpInstruction;
import com.neep.neepmeat.plc.instruction.PushInstruction;
import com.neep.neepmeat.plc.instruction.ReturnInstruction;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Instructions {
    public static final Map<String, InstructionProvider> REGISTRY = new HashMap<>();

    public static final InstructionProvider END = createTrivialBlock("end", Instruction.end());
    public static final InstructionProvider RESTART = createTrivialBlock("restart", SingletonInstructions.RESTART);
    public static final InstructionProvider PC = createTrivialBlock("pc", SingletonInstructions.PC);

    public static final SimplerInstructionProvider RET = createTrivialBlock("ret", ReturnInstruction.INSTANCE);
    public static final SimplerInstructionProvider CALL = register(new SimplerInstructionProvider((worldSupplier, nbt) -> new CallInstruction(nbt), new CallInstructionParser(), "CALL"));

    public static final SimplerInstructionProvider PUSH = register(new SimplerInstructionProvider(PushInstruction::new, new PushInstruction.Parser(), "PUSH"));
    public static final SimplerInstructionProvider POP = createTrivialBlock("pop", SingletonInstructions.POP);
    public static final SimplerInstructionProvider SWAP = createTrivialBlock("swp", SingletonInstructions.SWAP);
    public static final SimplerInstructionProvider DUP = createTrivialBlock("dup", SingletonInstructions.DUP);
    public static final SimplerInstructionProvider OVER = createTrivialBlock("over", SingletonInstructions.OVER);
    public static final SimplerInstructionProvider ROT = createTrivialBlock("rot", SingletonInstructions.ROT);
    public static final SimplerInstructionProvider PICK = createTrivialBlock("pick", SingletonInstructions.PICK);

    public static final SimplerInstructionProvider TO_RET = createTrivialBlock("rto", SingletonInstructions.TO_RET);
    public static final SimplerInstructionProvider FROM_RET = createTrivialBlock("rfrom", SingletonInstructions.FROM_RET);
    public static final SimplerInstructionProvider RET_FETCH = createTrivialBlock("rfetch", SingletonInstructions.RET_FETCH);
    public static final SimplerInstructionProvider RET_ADD = createTrivialBlock("radd", SingletonInstructions.RET_ADD);
    public static final SimplerInstructionProvider RET_EQ = createTrivialBlock("req", SingletonInstructions.RET_EQ);

    public static final SimplerInstructionProvider EQ = createTrivialBlock("eq", SingletonInstructions.EQ);
    public static final SimplerInstructionProvider LT = createTrivialBlock("lt", SingletonInstructions.LT);
    public static final SimplerInstructionProvider LTEQ = createTrivialBlock("lteq", SingletonInstructions.LTEQ);
    public static final SimplerInstructionProvider GT = createTrivialBlock("gt", SingletonInstructions.GT);
    public static final SimplerInstructionProvider GTEQ = createTrivialBlock("gteq", SingletonInstructions.GTEQ);

    public static final SimplerInstructionProvider INC = createTrivialBlock("inc", SingletonInstructions.INC);
    public static final SimplerInstructionProvider DEC = createTrivialBlock("dec", SingletonInstructions.DEC);
    public static final SimplerInstructionProvider NEG = createTrivialBlock("neg", SingletonInstructions.NEG);

    public static final SimplerInstructionProvider ADD = createTrivialBlock("add", SingletonInstructions.ADD);
    public static final SimplerInstructionProvider SUB = createTrivialBlock("sub", SingletonInstructions.SUB);
    public static final SimplerInstructionProvider MUL = createTrivialBlock("mul", SingletonInstructions.MUL);
    public static final SimplerInstructionProvider DIV = createTrivialBlock("div", SingletonInstructions.DIV);

    public static final SimplerInstructionProvider NOT = createTrivialBlock("not", SingletonInstructions.NOT);
    public static final SimplerInstructionProvider AND = createTrivialBlock("and", SingletonInstructions.AND);
    public static final SimplerInstructionProvider OR = createTrivialBlock("or", SingletonInstructions.OR);
    public static final SimplerInstructionProvider NAND = createTrivialBlock("nand", SingletonInstructions.NAND);
    public static final SimplerInstructionProvider NOR = createTrivialBlock("nor", SingletonInstructions.NOR);
    public static final SimplerInstructionProvider XOR = createTrivialBlock("xor", SingletonInstructions.XOR);
    public static final SimplerInstructionProvider XNOR = createTrivialBlock("xnor", SingletonInstructions.XNOR);

    public static final SimplerInstructionProvider TO_INT = createTrivialBlock("to_int", SingletonInstructions.TO_INT);
    public static final SimplerInstructionProvider TO_STR = createTrivialBlock("to_str", SingletonInstructions.TO_STR);

    public static final InstructionProvider JUMP = register(new SimplerInstructionProvider((worldSupplier, nbt) -> new JumpInstruction(nbt), new JumpInstructionParser(JumpInstruction::new), "JMP"));
    public static final SimplerInstructionProvider BIT = register(new SimplerInstructionProvider((world, nbt) -> new BITInstruction(nbt), new JumpInstructionParser(BITInstruction::new), "BIT"));
    public static final SimplerInstructionProvider BIF = register(new SimplerInstructionProvider((world, nbt) -> new BIFInstruction(nbt), new JumpInstructionParser(BIFInstruction::new), "BIF"));

    private static <T extends InstructionProvider> T register(T provider) {
        REGISTRY.put(provider.getParseName(), provider);
        return provider;
    }

    private static SimplerInstructionProvider createTrivialBlock(String path, Instruction instruction) {
        return register(new SimplerInstructionProvider((world, nbt) -> instruction,
                parseNoArguments(instruction),
                path.toLowerCase(Locale.ROOT)));
    }

    static InstructionParser parseNoArguments(Instruction instruction) {
        return (NeepAsmTokenView view, NeepAsmParser parser, @Nullable String scope) -> {
            view.fastForward();
            parser.assureLineEnd(view);
            return (ParsedInstruction) (source, program) -> instruction;
        };
    }

    public static CompoundTag labelToNbt(Label label) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("name", label.name());
        nbt.putInt("index", label.index());
        return nbt;
    }

    public static Label labelFromNbt(CompoundTag nbt) {
        return new Label(nbt.getString("name"), nbt.getInt("index"));
    }
}
