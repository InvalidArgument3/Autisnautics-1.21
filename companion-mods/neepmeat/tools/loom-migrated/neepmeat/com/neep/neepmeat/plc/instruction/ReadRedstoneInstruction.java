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
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.compiler.parser.PlcParsedInstruction;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.block.RedstoneInterface;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class ReadRedstoneInstruction implements PlcInstruction
{
    private final Argument target;
    private final LazyBlockApiCache<RedstoneInterface, Direction> redstoneCache;

    public ReadRedstoneInstruction(Supplier<Level> world, Argument argument)
    {
        this.target = argument;
        this.redstoneCache = LazyBlockApiCache.of(RedstoneInterface.LOOKUP, world, target);
    }

    public ReadRedstoneInstruction(Supplier<Level> world, CompoundTag nbt)
    {
        this.target = Argument.fromNbt(nbt.getCompound("target"));
        this.redstoneCache = LazyBlockApiCache.of(RedstoneInterface.LOOKUP, world, target);
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.put("target", target.toNbt());
        return nbt;
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        RedstoneInterface redstone = redstoneCache.find();
        if (redstone != null)
        {
            plc.dataStack().push(redstone.getReceivedStrength());
        }
        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.READ_REDSTONE;
    }

    public static PlcParsedInstruction parser(NeepAsmTokenView view, NeepAsmParser parser, String s) throws NeepASM.ParseException
    {
        view.fastForward();
        Argument target = parser.parseWorldTarget(view);
        if (target == null)
            throw new NeepASM.ParseException("expected redstone target");

        return ((world, source, program) ->
        {
            program.addBack(new ReadRedstoneInstruction(() -> world, target));
        });
    }
}
