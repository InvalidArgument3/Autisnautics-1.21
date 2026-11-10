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

package com.neep.neepmeat.api.plc.instruction;

import com.google.common.collect.Lists;
import com.neep.neepmeat.neepasm.program.KeyValue;
import com.neep.neepmeat.plc.instruction.*;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.level.Level;

public class PredicatedInstructionBuilder implements InstructionBuilder
{
    private final List<Argument> arguments = Lists.newArrayList();
    private final SimpleInstructionProvider provider;
    private final Level world;
    private final Consumer<Instruction> finished;
    private final List<ArgumentPredicate> predicates;

    PredicatedInstructionBuilder(SimpleInstructionProvider provider, Level world, Consumer<Instruction> finished, List<ArgumentPredicate> predicates)
    {
        this.provider = provider;
        this.world = world;
        this.finished = finished;
        this.predicates = predicates;
    }

    @Override
    public InstructionBuilder argument(Argument argument) throws InstructionException
    {
        predicates.get(arguments.size()).test(world, argument);

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
        return this;
    }

    public boolean isComplete()
    {
        return arguments.size() == provider.maxArguments();
    }

    @Override
    public Instruction build()
    {
        return provider.create(world, arguments);
    }

    @Override
    public int argumentCount()
    {
        return arguments.size();
    }

    public static Builder create()
    {
        return new Builder();
    }

    // It's an instruction builder builder. InstructionProvider is an instruction builder builder builder.
    public static class Builder implements InstructionBuilderFactory
    {
        private final List<ArgumentPredicate> predicates = Lists.newArrayList();

        public Builder arg(ArgumentPredicate predicate)
        {
            predicates.add(predicate);
            return this;
        }

        @Override
        public InstructionBuilder create(SimpleInstructionProvider provider, Level world, Consumer<Instruction> finished)
        {
            if (provider.maxArguments() != predicates.size())
                throw new IllegalStateException(provider.getShortName() + ": Argument and predicate count do not match. Fix your code!");

            return new PredicatedInstructionBuilder(provider, world, finished, predicates);
        }
    }
}
