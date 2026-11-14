package com.neep.neepmeat.plc.instruction.block;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.compiler.parser.PlcParsedInstruction;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.block.entity.BuilderBlockEntity;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import com.neep.neepmeat.plc.instruction.PlcInstruction;
import com.neep.neepmeat.plc.instruction.StockInstruction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;

public class SelItemInstruction implements PlcInstruction
{
    private final String pattern;

    public SelItemInstruction(String pattern)
    {
        this.pattern = pattern;
    }

    public SelItemInstruction(CompoundTag nbt)
    {
        this(nbt.getString("pattern"));
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.putString("pattern", pattern);
        return nbt;
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        plc.controlActuator(BuilderBlockEntity.class, builder ->
        {
            var pattern = !this.pattern.isEmpty() ? this.pattern : plc.dataStack().pop().stringValue();
            builder.selectItem(pattern);
        });
        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.SELITEM;
    }

    public static PlcParsedInstruction parser(NeepAsmTokenView view, NeepAsmParser parser, @Nullable String s) throws NeepASM.ParseException
    {
        view.fastForward();
        String string = Instructions.parseRegistryGlob1(view, BuiltInRegistries.ITEM, "item", true);
        parser.assureLineEnd(view);

        return (world, parsedSource1, program) ->
        {
            program.addBack(new SelItemInstruction(string));
        };
    }
}
