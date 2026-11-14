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
import com.neep.neepmeat.neepasm.vm.ThordData;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.block.entity.PLCBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class NeepBusReadInstruction implements PlcInstruction
{
    @Nullable private final String address;

    public NeepBusReadInstruction(Supplier<Level> world, CompoundTag nbt)
    {
        this(nbt.contains("address") ? nbt.getString("address") : null);
    }

    public NeepBusReadInstruction(@Nullable String address)
    {
        this.address = address;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        if (address != null)
            nbt.putString("address", address);
        return nbt;
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        if (plc instanceof PLCBlockEntity be)
        {
            String usedAddress = address;
            if (address == null)
                usedAddress = plc.dataStack().pop().stringValue();

            var read = be.getSender().read(usedAddress);
            plc.dataStack().push(ThordData.of(read));
        }

        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.NEEPBUS_READ;
    }

    public static PlcParsedInstruction parser(NeepAsmTokenView view, NeepAsmParser parser, @Nullable String s) throws NeepASM.ParseException
    {
        String address = view.nextString();

        parser.assureLineEnd(view);

        return (world, parsedSource1, program) -> program.addBack(new NeepBusReadInstruction(address));
    }
}
