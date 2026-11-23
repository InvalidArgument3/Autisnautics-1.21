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
import com.neep.neepmeat.plc.Instructions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class RobotInstruction implements PlcInstruction
{
    private final @Nullable Argument target;

    public RobotInstruction(@Nullable Argument target)
    {
        this.target = target;
    }

    public RobotInstruction(Supplier<Level> world, CompoundTag nbt)
    {
        if (nbt.contains("target"))
            this.target = Argument.fromNbt(nbt.getCompound("target"));
        else
            this.target = null;
    }

    public RobotInstruction(Supplier<Level> worldSupplier, List<Argument> arguments)
    {
        this.target = arguments.get(0);
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        if (target != null)
            nbt.put("target", target.toNbt());
        return nbt;
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        if (target != null)
            plc.selectActuator(target.pos());
        else
            plc.selectActuator(null);

        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.ROBOT;
    }

//    public static class Parser implements InstructionParser
//    {
//        @Override
//        public ParsedInstruction parse(NeepAsmTokenView view, LabelLookup parsedSource, com.neep.neepmeat.neepasm.compiler.Parser parser, @Nullable String scope) throws NeepASM.ParseException
//        {
//            view.fastForward();
//            Argument target = parser.parseArgument(view);
//            return ((world, source, program) ->
//            {
//                program.addBack(new RobotInstruction(target));
//            });
//        }
//    }
}
