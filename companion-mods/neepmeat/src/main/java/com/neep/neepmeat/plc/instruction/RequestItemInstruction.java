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

import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.compiler.parser.PlcParsedInstruction;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class RequestItemInstruction implements PlcInstruction
{
//    private final LazyBlockApiCache<Void, Void> routerCache;
    private final Argument to;
    private final Pattern pattern;

    public RequestItemInstruction(Supplier<Level> worldSupplier, Argument to, String pattern)
    {
//        routerCache = LazyBlockApiCache.of(MeatLib.VOID_LOOKUP, router.pos(), worldSupplier, () -> null);
        this.pattern = Pattern.compile(pattern);
        this.to = to;
    }

    public RequestItemInstruction(Supplier<Level> world, CompoundTag nbt)
    {
        this(world, Argument.fromNbt(nbt.getCompound("to")), nbt.getString("pattern"));
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.putString("pattern", pattern.pattern());
        nbt.put("to", to.toNbt());
        return nbt;
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        var stack = plc.dataStack();
        int amount = stack.pop().intValue();

        var pattern = !this.pattern.pattern().isEmpty() ? this.pattern : Pattern.compile(NeepAsmParser.convertToRegex(stack.pop().stringValue()));

        if (plc.getActuator() instanceof PipeDriverBlockEntity be)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                Predicate<ItemVariant> predicate = v -> Instructions.match(pattern, v.getItem());
                boolean satisfied = be.getNetwork(null).request(predicate, amount, to.pos(), to.face(), RoutingNetwork.RequestType.EXACT_AMOUNT, new RoutingNetwork.RequestContext(), transaction);

                if (satisfied)
                    transaction.commit();

                plc.dataStack().push(satisfied ? -1 : 0);
            }
        }
        else
        {
            throw new NeepASM.RuntimeException("Actuator is not a pipe driver");
        }

        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.REQUEST;
    }

    public static PlcParsedInstruction parser(NeepAsmTokenView view, NeepAsmParser parser) throws NeepASM.ParseException
    {
        view.fastForward();
        Argument to = parser.parseWorldTarget(view);
        if (to == null)
            throw new NeepASM.ParseException("expected output pipe world target");

        String regex = Instructions.parseRegistryGlob(view, BuiltInRegistries.ITEM, "item", true);

        view.fastForward();

        parser.assureLineEnd(view);

        return (world, parsedSource1, program) ->
        {
            program.addBack(new RequestItemInstruction(() -> world, to, regex));
        };
    }
}
