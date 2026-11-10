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

import com.neep.neepmeat.api.plc.PLC;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.neepmeat.neepasm.compiler.parser.PlcParsedInstruction;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class RouteItemInstruction implements PlcInstruction
{
    private final Argument from;
    private final Argument to;
    private final Pattern pattern;

    public RouteItemInstruction(Supplier<Level> worldSupplier, Argument from, Argument to, String pattern)
    {
        this.pattern = Pattern.compile(pattern);
        this.from = from;
        this.to = to;
    }

    public RouteItemInstruction(Supplier<Level> world, CompoundTag nbt)
    {
        this(world,
                Argument.fromNbt(nbt.getCompound("from")),
                Argument.fromNbt(nbt.getCompound("to")),
                nbt.getString("pattern")
        );
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.putString("pattern", pattern.pattern());
        nbt.put("from", from.toNbt());
        nbt.put("to", to.toNbt());
        return nbt;
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        var stack = plc.dataStack();

        int amount = stack.pop().intValue();

        if (plc.getActuator() instanceof PipeDriverBlockEntity be)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                Predicate<ItemVariant> predicate = v ->
                {
                    String id = v.getItem().builtInRegistryHolder().key().location().toString();
                    return pattern.asMatchPredicate().test(id);
                };

//                    ResourceAmount<ItemVariant> ra = new ResourceAmount<>(item, amount);
                boolean satisfied = be.getNetwork(null).route(predicate, amount, from.pos(), from.face(), to.pos(), to.face(), RoutingNetwork.RequestType.EXACT_AMOUNT, transaction);

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
        return Instructions.ROUTE;
    }

    public static PlcParsedInstruction parser(NeepAsmTokenView view, NeepAsmParser parser) throws NeepASM.ParseException
    {
        view.fastForward();
        Argument from = parser.parseWorldTarget(view);
        if (from == null)
            throw new NeepASM.ParseException("expected input pipe world target");

        view.fastForward();
        Argument to = parser.parseWorldTarget(view);
        if (to == null)
            throw new NeepASM.ParseException("expected output pipe world target");

        String notGlob = view.nextString();
        if (notGlob == null)
            throw new NeepASM.ParseException("expected item ID string (minecraft:stone)");

        view.fastForward();

        // If a normal string is given, verify it against all items in the registry.
        if (NeepAsmParser.isSimplePattern(notGlob))
        {
            Item item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.tryParse(notGlob)).orElse(null);
            if (item == null)
                throw new NeepASM.ParseException("item '" + notGlob + "' not known");
        }
        String regex = NeepAsmParser.convertToRegex(notGlob);

        parser.assureLineEnd(view);

        return (world, parsedSource1, program) ->
        {
            program.addBack(new RouteItemInstruction(() -> world, from, to, regex));
        };
    }

}
