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

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.api.plc.instruction.CallInstruction;
import com.neep.neepmeat.api.plc.instruction.PredicatedInstructionBuilder;
import com.neep.neepmeat.api.plc.instruction.SimpleInstructionProvider;
import com.neep.neepmeat.api.plc.instruction.SimplerInstructionProvider;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.compiler.parser.CallInstructionParser;
import com.neep.neepmeat.neepasm.compiler.parser.InstructionParser;
import com.neep.neepmeat.neepasm.compiler.parser.JumpInstructionParser;
import com.neep.neepmeat.neepasm.compiler.parser.ParsedInstruction;
import com.neep.neepmeat.neepasm.program.Label;
import com.neep.neepmeat.plc.instruction.*;
import com.neep.neepmeat.plc.instruction.block.SelItemInstruction;
import com.neep.neepmeat.plc.instruction.block.SelSlotInstruction;
import com.neep.neepmeat.plc.instruction.inventory.FindSlotInstruction;
import com.neep.neepmeat.plc.instruction.memory.ExtFetchInstruction;
import com.neep.neepmeat.plc.instruction.memory.ExtStoreInstruction;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.neep.neepmeat.neepasm.compiler.NeepAsmParser.convertToRegex;
import static com.neep.neepmeat.neepasm.compiler.NeepAsmParser.isSimplePattern;

public class Instructions
{
    public static final Map<String, InstructionProvider> REGISTRY = new HashMap<>();
//    public static final Registry<InstructionProvider> REGISTRY = FabricRegistryBuilder.createSimple(
//            InstructionProvider.class,
//            new Identifier(NeepMeat.NAMESPACE, "instruction_provider")).buildAndRegister();

    public static final InstructionProvider END = registerSingleton("end", Instruction.end());
    public static final InstructionProvider RESTART = registerSingleton("restart", SingletonInstructions.RESTART);
    public static final InstructionProvider PC = registerSingleton("pc", SingletonInstructions.PC);

    public static final SimplerInstructionProvider RET = registerSingleton("ret", ReturnInstruction.INSTANCE);
    public static final SimplerInstructionProvider CALL = register(new SimplerInstructionProvider((worldSupplier, nbt) -> new CallInstruction(nbt), new CallInstructionParser(), "CALL"));
    public static final SimplerInstructionProvider DYCALL = registerSingleton("dycall", SingletonInstructions.DYCALL);
    public static final SimplerInstructionProvider DYLOC = registerSingleton("dyloc", SingletonInstructions.DYLOC);
    public static final SimplerInstructionProvider IHANDLER = register(new SimplerInstructionProvider(InterruptHandlerInstruction::new, InterruptHandlerInstruction::parser, "IHANDLER"));
    public static final SimplerInstructionProvider IWAIT = registerSingleton("iwait", WaitForInterruptInstruction.INSTANCE);
    public static final SimplerInstructionProvider ICLEAR = registerSingleton("iclear", () -> Instructions.ICLEAR, WaitForInterruptInstruction::clearInterrupts);
    public static final SimplerInstructionProvider IDISABLE = registerSingleton("idisable", () -> Instructions.IDISABLE, WaitForInterruptInstruction::disableInterrupts);
    public static final SimplerInstructionProvider IENABLE = registerSingleton("ienable", () -> Instructions.IENABLE, WaitForInterruptInstruction::enableInterrupts);

    public static final SimplerInstructionProvider PUSH = register(new SimplerInstructionProvider(PushInstruction::new, new PushInstruction.Parser(), "PUSH"));
    public static final SimplerInstructionProvider POP = registerSingleton("pop", SingletonInstructions.POP);
    public static final SimplerInstructionProvider SWAP = registerSingleton("swp", SingletonInstructions.SWAP);
    public static final SimplerInstructionProvider DUP = registerSingleton("dup", SingletonInstructions.DUP);
    public static final SimplerInstructionProvider OVER = registerSingleton("over", SingletonInstructions.OVER);
    public static final SimplerInstructionProvider ROT = registerSingleton("rot", SingletonInstructions.ROT);
    public static final SimplerInstructionProvider PICK = registerSingleton("pick", SingletonInstructions.PICK);

    public static final SimplerInstructionProvider TO_RET = registerSingleton("rto", SingletonInstructions.TO_RET);
    public static final SimplerInstructionProvider FROM_RET = registerSingleton("rfrom", SingletonInstructions.FROM_RET);
    public static final SimplerInstructionProvider RET_FETCH = registerSingleton("rfetch", SingletonInstructions.RET_FETCH);
    public static final SimplerInstructionProvider RET_ADD = registerSingleton("radd", SingletonInstructions.RET_ADD);
    public static final SimplerInstructionProvider RET_EQ = registerSingleton("req", SingletonInstructions.RET_EQ);

    public static final InstructionProvider FREE = registerSingleton("free", SingletonInstructions.FREE);
    public static final InstructionProvider STORE = registerSingleton("store", SingletonInstructions.STORE);
    public static final InstructionProvider ALLOT = registerSingleton("allot", SingletonInstructions.ALLOT);
    public static final InstructionProvider FETCH = registerSingleton("fetch", SingletonInstructions.FETCH);
    public static final InstructionProvider EXTFETCH = register(new SimplerInstructionProvider(ExtFetchInstruction::new, ExtFetchInstruction::parser, "EXTFETCH"));
    public static final InstructionProvider EXTSTORE = register(new SimplerInstructionProvider(ExtStoreInstruction::new, ExtStoreInstruction::parser, "EXTSTORE"));
    public static final InstructionProvider FILL = registerSingleton("fill", SingletonInstructions.FILL);
//    public static final InstructionProvider ERASE = registerSingleton("erase", SingletonInstructions.ERASE);

    public static final SimplerInstructionProvider DELAY = register(new SimplerInstructionProvider(DelayInstruction::new, DelayInstruction::parse, "DELAY"));

    public static final SimplerInstructionProvider EQ = registerSingleton("eq", SingletonInstructions.EQ);
    public static final SimplerInstructionProvider LT = registerSingleton("lt", SingletonInstructions.LT);
    public static final SimplerInstructionProvider LTEQ = registerSingleton("lteq", SingletonInstructions.LTEQ);
    public static final SimplerInstructionProvider GT = registerSingleton("gt", SingletonInstructions.GT);
    public static final SimplerInstructionProvider GTEQ = registerSingleton("gteq", SingletonInstructions.GTEQ);

    public static final SimplerInstructionProvider INC = registerSingleton("inc", SingletonInstructions.INC);
    public static final SimplerInstructionProvider DEC = registerSingleton("dec", SingletonInstructions.DEC);
    public static final SimplerInstructionProvider NEG = registerSingleton("neg", SingletonInstructions.NEG);

    public static final SimplerInstructionProvider ADD = registerSingleton("add", SingletonInstructions.ADD);
    public static final SimplerInstructionProvider SUB = registerSingleton("sub", SingletonInstructions.SUB);
    public static final SimplerInstructionProvider MUL = registerSingleton("mul", SingletonInstructions.MUL);
    public static final SimplerInstructionProvider DIV = registerSingleton("div", SingletonInstructions.DIV);

    public static final SimplerInstructionProvider NOT = registerSingleton("not", SingletonInstructions.NOT);
    public static final SimplerInstructionProvider AND = registerSingleton("and", SingletonInstructions.AND);
    public static final SimplerInstructionProvider OR = registerSingleton("or", SingletonInstructions.OR);
    public static final SimplerInstructionProvider NAND = registerSingleton("nand", SingletonInstructions.NAND);
    public static final SimplerInstructionProvider NOR = registerSingleton("nor", SingletonInstructions.NOR);
    public static final SimplerInstructionProvider XOR = registerSingleton("xor", SingletonInstructions.XOR);
    public static final SimplerInstructionProvider XNOR = registerSingleton("xnor", SingletonInstructions.XNOR);

    public static final SimplerInstructionProvider SHR = register(new SimplerInstructionProvider(SHRInstruction::new, SHRInstruction::parser, "SHR"));
    public static final SimplerInstructionProvider SHL = register(new SimplerInstructionProvider(SHLInstruction::new, SHLInstruction::parser, "SHL"));

    // Type conversions
    public static final SimplerInstructionProvider TO_INT = registerSingleton("to_int", SingletonInstructions.TO_INT);
    public static final SimplerInstructionProvider TO_STR = registerSingleton("to_str", SingletonInstructions.TO_STR);

    public static final InstructionProvider JUMP = register(new SimplerInstructionProvider((worldSupplier, nbt) -> new JumpInstruction(nbt), new JumpInstructionParser(JumpInstruction::new), "JMP"));
    public static final SimplerInstructionProvider BIT = register(new SimplerInstructionProvider((w, n) -> new BITInstruction(n), new JumpInstructionParser(BITInstruction::new), "BIT"));
    public static final SimplerInstructionProvider BIF = register(new SimplerInstructionProvider((w, n) -> new BIFInstruction(n), new JumpInstructionParser(BIFInstruction::new), "BIF"));

    public static final SimplerInstructionProvider SAY = register(new SimplerInstructionProvider((w, n) -> new SayInstruction(n), new SayInstruction.Parser(), "SAY"));
    public static final InstructionProvider REMOVE = register(new SimpleInstructionProvider(RemoveInstruction::new, RemoveInstruction::new, 1, "REMOVE"));

    public static final InstructionProvider ROBOT = register(new SimpleInstructionProvider(RobotInstruction::new, RobotInstruction::new, 1, "ROBOT"))
            .factory(PredicatedInstructionBuilder.create()
                    .arg(ArgumentPredicates.IS_ACTUATOR));

    public static final InstructionProvider EXEC = register(new SimplerInstructionProvider(ExecInstruction::new, ExecInstruction::parser, "EXEC"));

    public static final InstructionProvider COMBINE = register(new SimpleInstructionProvider(CombineInstruction::new, CombineInstruction::new, 2, "COMBINE")
            .factory(PredicatedInstructionBuilder.create()
                    .arg(ArgumentPredicates.IS_ITEM_STORAGE)
                    .arg(ArgumentPredicates.IS_ITEM_MIP) ));
    public static final InstructionProvider MOVE = register(new SimpleInstructionProvider(MoveInstruction::new, MoveInstruction::new, 2, "MOVE")
            .factory(PredicatedInstructionBuilder.create()
                    .arg(ArgumentPredicates.IS_ITEM_STORAGE)
                    .arg(ArgumentPredicates.IS_ITEM_STORAGE)))
            .parser((view, parser, scope) -> MoveInstruction.parser(view, parser));
    public static final InstructionProvider IMPLANT = register(new SimpleInstructionProvider(ImplantInstruction::new, ImplantInstruction::new, 2, "IMPLANT")
            .factory(PredicatedInstructionBuilder.create()
                    .arg(ArgumentPredicates.IS_ITEM_STORAGE)
                    .arg(ArgumentPredicates.IS_ENTITY_MIP)));
    public static final InstructionProvider INJECT = register(new SimpleInstructionProvider(InjectInstruction::new, InjectInstruction::new, 2, "INJECT")
            .factory(PredicatedInstructionBuilder.create()
                    .arg(ArgumentPredicates.IS_FLUID_STORAGE)
                    .arg(ArgumentPredicates.IS_ITEM_ENTITY_MIP)));

    public static final SimplerInstructionProvider ROUTE = register(new SimplerInstructionProvider(RouteItemInstruction::new, (view, parser, scope) -> RouteItemInstruction.parser(view, parser), "ROUTE"));
    public static final SimplerInstructionProvider REQUEST = register(new SimplerInstructionProvider(RequestItemInstruction::new, (view, parser, scope) -> RequestItemInstruction.parser(view, parser), "REQUEST"));
    public static final SimplerInstructionProvider COUNT = register(new SimplerInstructionProvider(CountInstruction::new, (view, parser, scope) -> CountInstruction.parser(view, parser), "COUNT"));
    public static final SimplerInstructionProvider STOCK = register(new SimplerInstructionProvider(StockInstruction::new, StockInstruction::parser, "STOCK"));

    public static final InstructionProvider WAIT_REDSTONE = register(new SimplerInstructionProvider(WaitRedstoneInstruction::new, WaitRedstoneInstruction::parser, "RWAIT"));
    public static final InstructionProvider EMIT_REDSTONE = register(new SimplerInstructionProvider(EmitRedstoneInstruction::new, EmitRedstoneInstruction::parser, "REMIT"));
    public static final InstructionProvider READ_REDSTONE = register(new SimplerInstructionProvider(ReadRedstoneInstruction::new, ReadRedstoneInstruction::parser, "RREAD"));
    public static final InstructionProvider RISING_EDGE = register(new SimplerInstructionProvider((world, nbt) -> new RisingEdgeInstruction(nbt), RisingEdgeInstruction::parser, "RISING_EDGE"));

    public static final InstructionProvider NEEPBUS_WRITE = register(new SimplerInstructionProvider(NeepBusWriteInstruction::new, NeepBusWriteInstruction::parser, "NBWRITE"));
    public static final InstructionProvider NEEPBUS_READ = register(new SimplerInstructionProvider(NeepBusReadInstruction::new, NeepBusReadInstruction::parser, "NBREAD"));

    public static final InstructionProvider BPLACE = registerSingleton("bplace", SingletonInstructions.BPLACE);
    public static final InstructionProvider BBREAK = registerSingleton("bbreak", SingletonInstructions.BBREAK);
    public static final InstructionProvider MOVETO = registerSingleton("moveto", SingletonInstructions.MOVETO);

    public static final InstructionProvider FINDSLOT = register(new SimplerInstructionProvider((ws, nbt) -> new FindSlotInstruction(nbt), FindSlotInstruction::parser, "FINDSLOT"));
    public static final InstructionProvider SELSLOT = registerSingleton("selslot", SingletonInstructions.SELSLOT);
    public static final InstructionProvider SELITEM = register(new SimplerInstructionProvider((ws, nbt) -> new SelItemInstruction(nbt), SelItemInstruction::parser, "SELITEM"));

    public static final InstructionProvider AREA3 = registerSingleton("area3", SingletonInstructions.AREA3);

    private static <T extends InstructionProvider> T register(T provider)
    {
        REGISTRY.put(provider.getParseName(), provider);
        return provider;
    }

    private static <T extends SimplerInstructionProvider> SimplerInstructionProvider registerSingleton(String path, Instruction instruction)
    {
        return register(
                new SimplerInstructionProvider((w, n) -> instruction,
                parseNoArguments(instruction),
                path.toLowerCase(Locale.ROOT)));
    }

    private static <T extends SimplerInstructionProvider> SimplerInstructionProvider registerSingleton(String path, Supplier<InstructionProvider> supplier, Consumer<PLC> operation)
    {
        PlcInstruction instruction = new SimpleInstruction()
        {
            @Override
            public void start(PLC plc) throws NeepASM.RuntimeException
            {
                operation.accept(plc);
            }

            @Override
            public @NotNull InstructionProvider getOpcode()
            {
                return supplier.get();
            }
        };

        return registerSingleton(path, instruction);
    }

    /**
     * @param instruction Constructor of the desired instruction
     * @return An InstructionParser that expects a blank line (disregarding comments) after the operation.
     */
    static InstructionParser parseNoArguments(Instruction instruction)
    {
        return (NeepAsmTokenView view, NeepAsmParser parser, @Nullable String scope) ->
        {
            view.fastForward();
            parser.assureLineEnd(view);

            return (ParsedInstruction) (source, program) -> instruction;
        };
    }

    public static ResourceAmount<ItemVariant> takeItem(Argument target, Supplier<Level> world, int count, Pattern pattern)
    {
        var storage = ItemStorage.SIDED.find(world.get(), target.pos(), target.face());
        if (storage != null)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                ResourceAmount<ItemVariant> found = StorageUtil.findExtractableContent(storage, v -> match(pattern, v.getItem()), transaction);
                if (found != null)
                {
                    long extracted = storage.extract(found.resource(), Math.min(found.amount(), count), transaction);
                    if (extracted > 0)
                    {
                        var res = new ResourceAmount<>(found.resource(), extracted);
                        transaction.commit();
                        return res;
                    }

                    transaction.abort();
                }
            }
        }

        return null;
    }

    /// All or nothing
    public static ResourceAmount<FluidVariant> takeFluid(Argument target, Supplier<Level> world, long amount)
    {
        var storage = FluidStorage.SIDED.find(world.get(), target.pos(), target.face());
        if (storage != null)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                ResourceAmount<FluidVariant> found = StorageUtil.findExtractableContent(storage, transaction);
                if (found != null)
                {
                    long extracted = storage.extract(found.resource(), amount, transaction);
                    if (extracted == amount)
                    {
                        transaction.commit();
                        return new ResourceAmount<>(found.resource(), amount);
                    }

//                    long extracted = storage.extract(found.resource(), Math.min(found.amount(), amount), transaction);
//                    if (extracted > 0)
//                    {
//                        var res = new ResourceAmount<>(found.resource(), extracted);
//                        transaction.commit();
//                        return res;
//                    }
//
//                    transaction.abort();
                }
            }
        }

        return null;
    }

    public static long giveFluid(Argument target, Supplier<Level> world, ResourceAmount<FluidVariant> resourceAmount)
    {
        var storage = FluidStorage.SIDED.find(world.get(), target.pos(), target.face());
        if (storage != null)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                long inserted = storage.insert(resourceAmount.resource(), resourceAmount.amount(), transaction);
                transaction.commit();
                return inserted;
            }
        }

        return 0;
    }

    public static CompoundTag labelToNbt(Label label)
    {
        CompoundTag nbtCompound = new CompoundTag();
        nbtCompound.putString("name", label.name());
        nbtCompound.putInt("index", label.index());
        return nbtCompound;
    }

    public static Label labelFromNbt(CompoundTag nbt)
    {
        return new Label(nbt.getString("name"), nbt.getInt("index"));
    }

    public static boolean match(Pattern pattern, Item item)
    {
        return pattern.asMatchPredicate().test(item.builtInRegistryHolder().key().location().toString());
    }

    public static <T> String parseRegistryGlob(NeepAsmTokenView view, Registry<T> registry, String type, boolean allowNone) throws NeepASM.ParseException
    {
        return convertToRegex(parseRegistryGlob1(view, registry, type, allowNone));
    }

    public static <T> String parseRegistryGlob1(NeepAsmTokenView view, Registry<T> registry, String type, boolean allowNone) throws NeepASM.ParseException
    {
        view.fastForward();
        if (allowNone && view.peek() == '_')
        {
            view.next();
            return "";
        }

        String notGlob = view.nextString();
        if (notGlob == null)
            throw new NeepASM.ParseException("expected %s ID string (minecraft:stone)".formatted(type));

        // If a normal string is given, verify it against all items in the registry.
        if (isSimplePattern(notGlob))
        {
            T item = registry.getOptional(ResourceLocation.tryParse(notGlob)).orElse(null);
            if (item == null)
                throw new NeepASM.ParseException("%s '%s' not known".formatted(type, notGlob));
        }

        return notGlob;
    }
}
