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

import com.google.common.collect.Lists;
import com.neep.neepmeat.api.plc.instruction.InstructionBuilder;
import com.neep.neepmeat.api.plc.instruction.InstructionException;
import com.neep.neepmeat.api.plc.instruction.SimpleInstructionProvider;
import com.neep.neepmeat.neepasm.program.KeyValue;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.level.Level;

public class SimpleInstructionBuilder implements InstructionBuilder
{
    private final SimpleInstructionProvider provider;
    private final List<Argument> arguments = Lists.newArrayList();
    private final Level world;
    private final Consumer<Instruction> finished;

    public SimpleInstructionBuilder(SimpleInstructionProvider provider, Level world, Consumer<Instruction> finished)
    {
        this.provider = provider;
        this.world = world;
        this.finished = finished;

        if (isComplete())
        {
            finished.accept(build());
        }
    }

    public InstructionBuilder argument(Argument argument)
    {
        arguments.add(argument);
        if (isComplete())
        {
            finished.accept(build());
            arguments.clear();
        }
        return this;
    }

    @Override
    public InstructionBuilder keyValue(KeyValue kv) throws InstructionException
    {
        return null;
    }

    public boolean isComplete()
    {
        return arguments.size() >= provider.maxArguments();
    }

    public Instruction build()
    {
        return provider.create(world, arguments);
    }

    @Override
    public int argumentCount()
    {
        return arguments.size();
    }
}
