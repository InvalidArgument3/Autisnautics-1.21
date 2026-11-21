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

import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.meatgun.SlotType;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class SimpleModuleSlot implements ModuleSlot
{
    protected final Matrix4f matrix;
    private final RootModuleHolder.Listener listener;
    private MeatgunModule module = MeatgunModule.DEFAULT;
    private final boolean shouldRender;

    private final EnumSet<SlotType> slotType;

    public SimpleModuleSlot(RootModuleHolder.Listener listener, Matrix4f matrix, SlotType slotType, boolean shouldRender)
    {
        this(listener, matrix, EnumSet.of(slotType), shouldRender);
    }

    public SimpleModuleSlot(RootModuleHolder.Listener listener, Matrix4f matrix, EnumSet<SlotType> slotType, boolean shouldRender)
    {
        this.listener = listener;
        this.matrix = new Matrix4f(matrix);
        this.slotType = slotType;
        this.shouldRender = shouldRender;
    }

    public SimpleModuleSlot(RootModuleHolder.Listener listener, Matrix4f matrix)
    {
        this(listener, matrix, SlotType.NORMAL, true);
    }

    @Override
    public boolean shouldRender()
    {
        return shouldRender;
    }

    @Override
    public @NotNull MeatgunModule get()
    {
        return module;
    }

    @Override
    public void set(MeatgunModule module)
    {
        this.module = module;
        module.setTransform(new Matrix4f(matrix));
        listener.markDirty(RootModuleHolder.Reason.MODULE_SWAPPED);
    }

//    public SlotType getSlotType()
//    {
//        return slotType;
//    }

    public Set<SlotType> getApplicable()
    {
        return slotType;
    }

    @Override
    public Matrix4f transform()
    {
        return new Matrix4f(matrix);
    }

    @Override
    public Matrix4f transform(float tickDelta)
    {
        return new Matrix4f(matrix);
    }

    @Override
    public boolean applicable(MeatgunModule.Type<?> module)
    {
        return slotType.stream().anyMatch(t -> module.attributes().isApplicable(t));
    }
}
