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
import com.neep.neepmeat.api.plc.instruction.SimpleInstructionProvider;
import com.neep.neepmeat.api.plc.instruction.SimplerInstructionProvider;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.compiler.parser.CallInstructionParser;
import com.neep.neepmeat.neepasm.compiler.parser.InstructionParser;
import com.neep.neepmeat.neepasm.compiler.parser.JumpInstructionParser;
import com.neep.neepmeat.neepasm.compiler.parser.ParsedInstruction;
import com.neep.neepmeat.neepasm.program.Label;
import com.neep.neepmeat.plc.instruction.BIFInstruction;
import com.neep.neepmeat.plc.instruction.BITInstruction;
import com.neep.neepmeat.plc.instruction.Argument;
import com.neep.neepmeat.plc.instruction.EmitRedstoneInstruction;
import com.neep.neepmeat.plc.instruction.FreeInstruction;
import com.neep.neepmeat.plc.instruction.InjectInstruction;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.plc.instruction.MoveInstruction;
import com.neep.neepmeat.plc.instruction.ReadRedstoneInstruction;
import com.neep.neepmeat.plc.instruction.block.BreakBlockInstruction;
import com.neep.neepmeat.plc.instruction.block.MoveToInstruction;
import com.neep.neepmeat.plc.instruction.block.PlaceBlockInstruction;
import com.neep.neepmeat.plc.instruction.block.SelSlotInstruction;
import com.neep.neepmeat.plc.instruction.memory.AllotInstruction;
import com.neep.neepmeat.plc.instruction.memory.FetchInstruction;
import com.neep.neepmeat.plc.instruction.memory.FillInstruction;
import com.neep.neepmeat.plc.instruction.memory.StoreInstruction;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import com.neep.neepmeat.plc.instruction.JumpInstruction;
import com.neep.neepmeat.plc.instruction.PushInstruction;
import com.neep.neepmeat.plc.instruction.ReturnInstruction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

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

    public static final InstructionProvider FREE = createTrivialBlock("free", new FreeInstruction());
    public static final InstructionProvider STORE = createTrivialBlock("store", new StoreInstruction());
    public static final InstructionProvider ALLOT = createTrivialBlock("allot", new AllotInstruction());
    public static final InstructionProvider FETCH = createTrivialBlock("fetch", new FetchInstruction());
    public static final InstructionProvider FILL = createTrivialBlock("fill", new FillInstruction());

    public static final InstructionProvider MOVE = register(new SimplerInstructionProvider(MoveInstruction::new, (view, parser, scope) -> MoveInstruction.parser(view, parser), "MOVE"));
    public static final InstructionProvider INJECT = register(new SimpleInstructionProvider(InjectInstruction::new, InjectInstruction::new, 2, "INJECT"));
    public static final InstructionProvider MOVETO = createTrivialBlock("moveto", new MoveToInstruction());
    public static final InstructionProvider BPLACE = createTrivialBlock("bplace", new PlaceBlockInstruction());
    public static final InstructionProvider BBREAK = createTrivialBlock("bbreak", new BreakBlockInstruction());
    public static final InstructionProvider SELSLOT = createTrivialBlock("selslot", new SelSlotInstruction());
    public static final InstructionProvider READ_REDSTONE = register(new SimplerInstructionProvider(ReadRedstoneInstruction::new, ReadRedstoneInstruction::parser, "RREAD"));
    public static final InstructionProvider EMIT_REDSTONE = register(new SimplerInstructionProvider(EmitRedstoneInstruction::new, EmitRedstoneInstruction::parser, "REMIT"));

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

    public static ItemStack takeItem(Argument target, Supplier<Level> world, int count, Pattern pattern) {
        IItemHandler handler = itemHandler(world.get(), target);
        if (handler == null) {
            return ItemStack.EMPTY;
        }
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty() || !match(pattern, stack.getItem())) {
                continue;
            }
            ItemStack extracted = handler.extractItem(slot, Math.min(count, stack.getCount()), false);
            if (!extracted.isEmpty()) {
                return extracted;
            }
        }
        return ItemStack.EMPTY;
    }

    public static int giveItem(Argument target, Supplier<Level> world, ItemStack stack) {
        IItemHandler handler = itemHandler(world.get(), target);
        if (handler == null || stack.isEmpty()) {
            return 0;
        }
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = handler.insertItem(slot, remaining, false);
        }
        return stack.getCount() - remaining.getCount();
    }

    public static FluidStack takeFluid(Argument target, Supplier<Level> world, int amount) {
        IFluidHandler handler = fluidHandler(world.get(), target);
        if (handler == null) {
            return FluidStack.EMPTY;
        }
        FluidStack simulated = handler.drain(amount, IFluidHandler.FluidAction.SIMULATE);
        if (simulated.getAmount() != amount) {
            return FluidStack.EMPTY;
        }
        return handler.drain(amount, IFluidHandler.FluidAction.EXECUTE);
    }

    public static int giveFluid(Argument target, Supplier<Level> world, FluidStack stack) {
        IFluidHandler handler = fluidHandler(world.get(), target);
        if (handler == null || stack == null || stack.isEmpty()) {
            return 0;
        }
        return handler.fill(stack, IFluidHandler.FluidAction.EXECUTE);
    }

    private static IItemHandler itemHandler(Level level, Argument target) {
        return level.getCapability(Capabilities.ItemHandler.BLOCK, target.pos(), target.face());
    }

    private static IFluidHandler fluidHandler(Level level, Argument target) {
        return level.getCapability(Capabilities.FluidHandler.BLOCK, target.pos(), target.face());
    }

    public static boolean match(Pattern pattern, Item item) {
        return pattern.asMatchPredicate().test(BuiltInRegistries.ITEM.getKey(item).toString());
    }

    public static <T> String parseRegistryGlob(NeepAsmTokenView view, Registry<T> registry, String type, boolean allowNone) throws NeepASM.ParseException {
        return NeepAsmParser.convertToRegex(parseRegistryGlob1(view, registry, type, allowNone));
    }

    public static <T> String parseRegistryGlob1(NeepAsmTokenView view, Registry<T> registry, String type, boolean allowNone) throws NeepASM.ParseException {
        view.fastForward();
        if (allowNone && view.last() == '_') {
            view.next();
            return "";
        }
        String notGlob = view.nextString();
        if (notGlob == null) {
            throw new NeepASM.ParseException("expected %s ID string (minecraft:stone)".formatted(type));
        }
        if (NeepAsmParser.isSimplePattern(notGlob)) {
            ResourceLocation id = ResourceLocation.tryParse(notGlob);
            if (id == null || registry.getOptional(id).isEmpty()) {
                throw new NeepASM.ParseException("%s '%s' not known".formatted(type, notGlob));
            }
        }
        return notGlob;
    }
    public static final InstructionProvider AREA3 = createTrivialBlock("area3", Instruction.end());
    public static final InstructionProvider COMBINE = createTrivialBlock("combine", Instruction.end());
    public static final InstructionProvider COUNT = createTrivialBlock("count", Instruction.end());
    public static final InstructionProvider DELAY = createTrivialBlock("delay", Instruction.end());
    public static final InstructionProvider DYCALL = createTrivialBlock("dycall", Instruction.end());
    public static final InstructionProvider DYLOC = createTrivialBlock("dyloc", Instruction.end());
    public static final InstructionProvider EXEC = createTrivialBlock("exec", Instruction.end());
    public static final InstructionProvider EXTFETCH = createTrivialBlock("extfetch", Instruction.end());
    public static final InstructionProvider EXTSTORE = createTrivialBlock("extstore", Instruction.end());
    public static final InstructionProvider FINDSLOT = createTrivialBlock("findslot", Instruction.end());
    public static final InstructionProvider ICLEAR = createTrivialBlock("iclear", Instruction.end());
    public static final InstructionProvider IDISABLE = createTrivialBlock("idisable", Instruction.end());
    public static final InstructionProvider IENABLE = createTrivialBlock("ienable", Instruction.end());
    public static final InstructionProvider IHANDLER = createTrivialBlock("ihandler", Instruction.end());
    public static final InstructionProvider IMPLANT = createTrivialBlock("implant", Instruction.end());
    public static final InstructionProvider IWAIT = createTrivialBlock("iwait", Instruction.end());
    public static final InstructionProvider NEEPBUS_READ = createTrivialBlock("neepbus_read", Instruction.end());
    public static final InstructionProvider NEEPBUS_WRITE = createTrivialBlock("neepbus_write", Instruction.end());
    public static final InstructionProvider REMOVE = createTrivialBlock("remove", Instruction.end());
    public static final InstructionProvider REQUEST = createTrivialBlock("request", Instruction.end());
    public static final InstructionProvider RISING_EDGE = createTrivialBlock("rising_edge", Instruction.end());
    public static final InstructionProvider ROBOT = createTrivialBlock("robot", Instruction.end());
    public static final InstructionProvider ROUTE = createTrivialBlock("route", Instruction.end());
    public static final InstructionProvider SAY = createTrivialBlock("say", Instruction.end());
    public static final InstructionProvider SELITEM = createTrivialBlock("selitem", Instruction.end());
    public static final InstructionProvider SHL = createTrivialBlock("shl", Instruction.end());
    public static final InstructionProvider SHR = createTrivialBlock("shr", Instruction.end());
    public static final InstructionProvider STOCK = createTrivialBlock("stock", Instruction.end());
    public static final InstructionProvider WAIT_REDSTONE = createTrivialBlock("wait_redstone", Instruction.end());
}
