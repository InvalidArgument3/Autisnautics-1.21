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

import com.neep.neepmeat.neepasm.compiler.parser.DefaultInstructionParser;
import com.neep.neepmeat.neepasm.compiler.parser.InstructionParser;
import com.neep.neepmeat.plc.instruction.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SimpleInstructionProvider implements InstructionProvider
{
    protected final int arguments;
    protected final Constructor constructor;
    protected final NbtConstructor nbtConstructor;
    protected final String shortName;
    protected InstructionBuilderFactory factory = SimpleInstructionBuilder::new;
    protected InstructionParser parser = new DefaultInstructionParser(this);

    public SimpleInstructionProvider(Constructor constructor, NbtConstructor nbtConstructor, int arguments, String shortName)
    {
        this.constructor = constructor;
        this.nbtConstructor = nbtConstructor;
        this.arguments = arguments;
        this.shortName = shortName.toLowerCase(Locale.ROOT);
    }

    public SimpleInstructionProvider factory(InstructionBuilderFactory factory)
    {
        this.factory = factory;
        return this;
    }

    public SimpleInstructionProvider parser(InstructionParser parser)
    {
        this.parser = parser;
        return this;
    }

    public InstructionBuilder start(ServerLevel world, Consumer<Instruction> finished)
    {
        return factory.create(this, world, finished);
    }

    public int maxArguments()
    {
        return arguments;
    }

    public String getShortName()
    {
        return shortName.toUpperCase();
    }

    public String getParseName()
    {
        return shortName;
    }

    @FunctionalInterface
    public interface Constructor
    {
        Instruction create(Supplier<Level> world, List<Argument> arguments);
    }

    @FunctionalInterface
    public interface NbtConstructor
    {
        Instruction create(Supplier<Level> world, CompoundTag nbt);
    }

    public Instruction create(Level world, List<Argument> arguments)
    {
        return constructor.create(() -> world, arguments);
    }

    @Override
    public InstructionParser getParser()
    {
        return parser;
    }

    @Override
    public Instruction loadStatic(Supplier<Level> world, CompoundTag nbt)
    {
        return nbtConstructor.create(world, nbt);
    }
}
