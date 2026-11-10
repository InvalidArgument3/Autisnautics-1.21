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

package com.neep.neepmeat.plc.program;

import com.neep.neepmeat.api.plc.program.MutableProgram;
import com.neep.neepmeat.neepasm.program.Label;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

public class PLCProgramImpl implements MutableProgram
{
    private final Supplier<Level> worldSupplier;
    private final List<Instruction> instructions = new ObjectArrayList<>();
    private final Object2IntMap<String> exportedSymbols = new Object2IntOpenHashMap<>();

    private final IntList debugLines = new IntArrayList();
    private int debugLine = -1;

    private String name = "";

    public PLCProgramImpl(Supplier<Level> worldSupplier)
    {
        this.worldSupplier = worldSupplier;
    }

    public PLCProgramImpl(Supplier<Level> worldSupplier, CompoundTag nbt)
    {
        this.worldSupplier = worldSupplier;
        readNbt(nbt);
    }

    public static String getName(CompoundTag nbt)
    {
        return nbt.getString("name");
    }

    @Override
    public Instruction get(int offset)
    {
        if (offset < instructions.size())
        {
            return instructions.get(offset);
        }
        return Instruction.end();
    }

    @Override
    public int size()
    {
        return instructions.size();
    }

    @Override
    public int getDebugLine(int counter)
    {
        if (counter < 0 || counter >= debugLines.size())
            return -1;

        return debugLines.getInt(counter);
    }

    @Override
    public void addBack(Instruction instruction)
    {
        debugLines.add(debugLine);
        instructions.add(instruction);
    }

    @Override
    public void setDebugLine(int line)
    {
        debugLine = line;
    }

    @Override
    public void addSymbol(Label symbol)
    {
        exportedSymbols.put(symbol.name(), symbol.index());
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        ListTag list = new ListTag();
        for (int i = 0; i < instructions.size(); ++i)
        {
            Instruction instruction = instructions.get(i);
            int line = debugLines.getInt(i);

            CompoundTag instructionNbt = new CompoundTag();
            instruction.writeNbt(instructionNbt);
            instructionNbt.putInt("debug_line", line);
            instructionNbt.putString("id", instruction.getOpcode().getParseName());
            list.add(instructionNbt);
        }
        nbt.put("instructions", list);

        nbt.put("symbols", writeSymbols(exportedSymbols));

        nbt.putString("name", name);

        return nbt;
    }

    private static ListTag writeSymbols(Object2IntMap<String> symbols)
    {
        return symbols.object2IntEntrySet().stream().map(PLCProgramImpl::writeExportedSymbol)
                        .collect(ListTag::new, AbstractList::add, Collections::addAll);
    }

    private static void readSymbols(ListTag nbt, Object2IntMap<String> out)
    {
        out.clear();

        nbt.forEach(e ->
        {
            var c = ((CompoundTag) e);
            out.put(c.getString("name"), c.getInt("offset"));
        });
    }

    private static CompoundTag writeExportedSymbol(Object2IntMap.Entry<String> e)
    {
        var c = new CompoundTag();
        c.putString("name", e.getKey());
        c.putInt("offset", e.getIntValue());
        return c;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        instructions.clear();

        ListTag list = nbt.getList("instructions", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i)
        {
            CompoundTag instructionNbt = list.getCompound(i);

            int line = instructionNbt.getInt("debug_line");
//            Identifier id = Identifier.tryParse(instructionNbt.getString("id"));
            InstructionProvider provider = Instructions.REGISTRY.get(instructionNbt.getString("id"));
            if (provider != null)
            {
                instructions.add(provider.createFromNbt(worldSupplier, instructionNbt));
                debugLines.add(line);
            }
        }

        readSymbols(nbt.getList("symbols", Tag.TAG_COMPOUND), exportedSymbols);

        this.name = nbt.getString("name");
    }

    @Override
    public Map<String, Integer> exportedSymbols()
    {
        return exportedSymbols;
    }

    public void setName(String name)
    {
        exportedSymbols.removeInt(this.name);
        this.name = name;
        if (!this.name.isEmpty())
            exportedSymbols.put(this.name, 0);
    }

    @Override
    public String getName()
    {
        return name;
    }
}
