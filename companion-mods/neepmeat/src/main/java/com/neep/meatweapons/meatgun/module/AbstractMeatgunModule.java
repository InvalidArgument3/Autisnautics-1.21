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

package com.neep.meatweapons.meatgun.module;

import com.neep.meatweapons.meatgun.ModuleTransform;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public abstract class AbstractMeatgunModule implements MeatgunModule
{
    protected ModuleTransform transform = new ModuleTransform();
    protected List<ModuleSlot> slots;
    protected UUID uuid = UUID.randomUUID();
    protected final RootModuleHolder.Listener listener;

    public AbstractMeatgunModule(RootModuleHolder.Listener listener, List<ModuleSlot> slots)
    {
        this.listener = listener;
        this.slots = Collections.unmodifiableList(slots);
    }

    public AbstractMeatgunModule(RootModuleHolder.Listener listener)
    {
        this.listener = listener;
        this.slots = Collections.emptyList();
    }

    /// For use in constructor
    protected void setSlots(List<ModuleSlot> slots)
    {
        this.slots = Collections.unmodifiableList(slots);
    }

    @Override
    public UUID getUuid()
    {
        return uuid;
    }

    @Override
    public void setTransform(Matrix4f transform)
    {
        this.transform.set(transform);
    }

    @Override
    public List<ModuleSlot> getChildren()
    {
        return slots;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.putUUID("uuid", uuid);

        float[] fs = transform.transform.get(new float[16]);
        nbt.putIntArray("transform", IntStream.range(0, fs.length).map(i -> Float.floatToIntBits(fs[i])).toArray());

        ListTag slotsNbt = new ListTag();
        for (var slot : slots)
        {
            CompoundTag slotNbt = MeatgunModule.toNbt(slot.get());
            slotsNbt.add(slotNbt);
        }
        nbt.put("slots", slotsNbt);


        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt, @Nullable Matrix4f global)
    {
        if (nbt.hasUUID("uuid"))
            this.uuid = nbt.getUUID("uuid");

        int[] bits = nbt.getIntArray("transform");
        if (bits.length == 16) // Prevent crash if tag is empty.
        {
            float[] fs = new float[bits.length];
            for (int i = 0; i < bits.length; ++i)
                fs[i] = Float.intBitsToFloat(bits[i]);

            transform.set(fs);
        }
        else
        {
            transform.reset();
        }

        if (global != null)
            transform.setParent(global);
        // Otherwise, retain the full transform from before

        ListTag list = nbt.getList("slots", Tag.TAG_COMPOUND);
        for (int i = 0; i < slots.size(); ++i)
        {
            Type<?> preType = slots.get(i).get().getType();
            CompoundTag slotNbt = list.getCompound(i);
            Type<?> type = MeatgunModule.readType(slotNbt);
            if (type != preType)
            {
                MeatgunModule module = type.create(listener, slotNbt);
                module.readNbt(slotNbt, transform.globalTransform);
                slots.get(i).set(module);
            }
            else
            {
                slots.get(i).get().readNbt(slotNbt, transform.globalTransform);
            }
        }
    }
}
