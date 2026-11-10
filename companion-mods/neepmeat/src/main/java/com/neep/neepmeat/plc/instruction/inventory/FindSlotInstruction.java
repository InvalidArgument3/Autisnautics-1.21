package com.neep.neepmeat.plc.instruction.inventory;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.api.storage.AutoInvalidatingLazyBlockApiCache;
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.compiler.parser.PlcParsedInstruction;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import com.neep.neepmeat.plc.instruction.PlcInstruction;
import com.neep.neepmeat.plc.instruction.StockInstruction;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class FindSlotInstruction implements PlcInstruction
{
    private final LazyBlockApiCache<Storage<ItemVariant>, Direction> cache;
    private Level world;
    private BlockPos currentPos;
    private Direction currentDir;

    private final Pattern pattern;

    public FindSlotInstruction(String pattern)
    {
        this.cache = new AutoInvalidatingLazyBlockApiCache<>(() -> this.world, ItemStorage.SIDED, () -> this.currentPos, () -> this.currentDir);
        this.pattern = Pattern.compile(pattern);
    }

    public FindSlotInstruction(CompoundTag nbt)
    {
        this(nbt.getString("pattern"));
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.putString("pattern", pattern.pattern());
        return nbt;
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        if (plc instanceof BlockEntity be)
        {
            var stack = plc.dataStack();
            var argument = stack.pop().worldTargetValue();
            this.world = be.getLevel();
            this.currentPos = argument.pos();
            this.currentDir = argument.face();

            stack.push(find(plc));
            plc.advanceCounter();
        }
        else
        {
            throw new NeepASM.RuntimeException("%s not supported by processor".formatted(getOpcode().getParseName()));
        }
    }

    private int find(PLC plc) throws NeepASM.RuntimeException
    {
        var storage = cache.find();
        var pattern = StockInstruction.handlePossibleEmpty(plc.dataStack(), this.pattern);

        if (storage != null)
        {
            int i = 0;
            for (var view : storage)
            {
                try (Transaction transaction = Transaction.openOuter())
                {
                    Predicate<ItemVariant> predicate = v -> Instructions.match(pattern, v.getItem());

                    if (predicate.test(view.getResource()))
                    {
                        return i;
                    }

                    transaction.abort();
                }
                i++;
            }
        }

        return -1;
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.FINDSLOT;
    }

    public static PlcParsedInstruction parser(NeepAsmTokenView view, NeepAsmParser parser, @Nullable String s) throws NeepASM.ParseException
    {
        view.fastForward();
        String regex = Instructions.parseRegistryGlob(view, BuiltInRegistries.ITEM, "item", true);
        parser.assureLineEnd(view);

        return (world, parsedSource1, program) ->
        {
            program.addBack(new FindSlotInstruction(regex));
        };
    }
}
