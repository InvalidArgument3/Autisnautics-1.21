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
import com.neep.neepmeat.api.plc.interrupt.InterruptEmitter;
import com.neep.neepmeat.api.plc.interrupt.InterruptReceiver;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.compiler.ParsedSource;
import com.neep.neepmeat.neepasm.compiler.parser.PlcParsedInstruction;
import com.neep.neepmeat.neepasm.program.Label;
import com.neep.neepmeat.plc.Instructions;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class InterruptHandlerInstruction implements PlcInstruction
{
    private final Supplier<Level> worldSupplier;
    private final Argument target;
    private final Label label;

    public InterruptHandlerInstruction(Supplier<Level> worldSupplier, CompoundTag nbt)
    {
        this(worldSupplier, Argument.fromNbt(nbt.getCompound("target")),
                new Label(nbt.getString("label_name"), nbt.getInt("label_index")));
    }

    public InterruptHandlerInstruction(Supplier<Level> worldSupplier, Argument target, Label label)
    {
        this.worldSupplier = worldSupplier;
        this.target = target;
        this.label = label;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.put("target", target.toNbt());
        nbt.putString("label_name", label.name());
        nbt.putInt("label_index", label.index());
        return nbt;
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        if (plc instanceof InterruptReceiver interruptReceiver)
        {
            if (worldSupplier.get().getBlockEntity(target.pos()) instanceof InterruptEmitter)
            {
                interruptReceiver.getInterrupts().put(target.pos(), label);
            }
            else
            {
                throw new NeepASM.RuntimeException("IHANDLER: Target does not emit interrupts");
            }
        }

        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.IHANDLER;
    }

    public static PlcParsedInstruction parser(NeepAsmTokenView view, NeepAsmParser parser, String scope) throws NeepASM.ParseException
    {
        view.fastForward();

        Argument target = parser.parseWorldTarget(view);
        if (target == null)
            throw new NeepASM.ParseException("expected world target");

        view.fastForward();

        String id = view.nextIdentifier();

        if (id.isEmpty())
            throw new NeepASM.ParseException("expected label");

        String label = ParsedSource.mangleLabel(id, scope);

        parser.assureLineEnd(view);

        return (world, parsedSource1, program) ->
        {
            Label l;
            if (label.equals("_"))
                l = Label.IGNORED;
            else
                l = parsedSource1.findLabel(label, program.size(), Label.Seek.ABSOLUTE);

            if (l == null)
                throw new NeepASM.CompilationException("label '" + label + "' does not exist");

            program.addBack(new InterruptHandlerInstruction(() -> world, target, l));
        };
    }
}
