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

package com.neep.neepmeat.plc.robot;

import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.api.plc.interrupt.InterruptEmitter;
import com.neep.neepmeat.api.plc.interrupt.InterruptReceiver;
import com.neep.neepmeat.neepasm.program.Label;
import com.neep.neepmeat.plc.Instructions;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

public class InterruptManager implements NbtSerialisable
{
    private final Map<BlockPos, Label> table = new HashMap<>();
    private final Supplier<Level> worldSupplier;
    private final InterruptReceiver receiver;
    private final Runnable markDirty;

    private final Deque<Label> interruptQueue = new ArrayDeque<>();

    private boolean needsUpdate = true;
    private boolean enabled = true;

    // Prevent possible memory leaks from naughty players
    private final int maxEvents = 32;

    public InterruptManager(Supplier<Level> worldSupplier, InterruptReceiver receiver, Runnable markDirty)
    {
        this.worldSupplier = worldSupplier;
        this.receiver = receiver;
        this.markDirty = markDirty;
    }

    public void put(BlockPos pos, Label label)
    {
        table.put(pos, label);
        if (worldSupplier.get().getBlockEntity(pos) instanceof InterruptEmitter emitter)
        {
            emitter.addListener(receiver);
        }

        markDirty.run();
    }

    public void remove(BlockPos pos)
    {
        table.remove(pos);
    }

    public void clear()
    {
        table.clear();
        interruptQueue.clear();
    }

    public void update()
    {
        if (!needsUpdate)
            return;

        needsUpdate = false;
        for (BlockPos pos : table.keySet())
        {
            if (worldSupplier.get().getBlockEntity(pos) instanceof InterruptEmitter emitter)
            {
                emitter.addListener(receiver);
            }
        }
    }

    public boolean contains(BlockPos pos)
    {
        return table.containsKey(pos);
    }

//    @Nullable
//    public Label get(BlockPos pos)
//    {
//        return table.get(pos);
//    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.putBoolean("enabled", enabled);
        ListTag list = new ListTag();
        table.forEach((p, l) ->
        {
            CompoundTag nbtCompound = new CompoundTag();
            nbtCompound.put("pos", NbtUtils.writeBlockPos(p));
            nbtCompound.put("label", Instructions.labelToNbt(l));
            list.add(nbtCompound);
        });

        nbt.put("interrupt_table", list);

        ListTag queue = new ListTag();
        for (var entry : interruptQueue)
        {
            queue.add(Instructions.labelToNbt(entry));
        }
        nbt.put("queue", queue);

        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        table.clear();
        this.enabled = nbt.getBoolean("enabled");
        ListTag list = nbt.getList("interrupt_table", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i)
        {
            CompoundTag compound = list.getCompound(i);
            BlockPos pos = NbtUtils.readBlockPos(compound, "pos").orElse(net.minecraft.core.BlockPos.ZERO);
            Label label = Instructions.labelFromNbt(compound.getCompound("label"));
            table.put(pos, label);
        }

        var queue = nbt.getList("queue", Tag.TAG_COMPOUND);
        interruptQueue.clear();
        for (int i = 0; i < queue.size(); i++)
        {
            interruptQueue.add(Instructions.labelFromNbt(queue.getCompound(i)));
        }

        needsUpdate = true;
    }

    public void interrupt(BlockPos pos, InterruptEmitter emitter)
    {
        if (interruptQueue.size() > maxEvents)
            return;

        Label label = table.get(pos);
        if (label == null)
            return;

        interruptQueue.add(label);
    }

    public Label peek()
    {
        return interruptQueue.peek();
    }

    public Label poll()
    {
        return interruptQueue.poll();
    }

    public boolean hasQueued()
    {
        return enabled && !interruptQueue.isEmpty();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        // Prevents interrupt spam but causes events to be left unhandled
        // I think having to handle a long queue of interrupts is preferable to missed events
//        if (!enabled)
//        {
//            interruptQueue.clear();
//        }

        this.enabled = enabled;
    }
}
