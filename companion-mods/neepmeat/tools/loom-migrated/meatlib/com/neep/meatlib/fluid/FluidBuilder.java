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

package com.neep.meatlib.fluid;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.CreativeModeTab;
import org.jetbrains.annotations.Nullable;

public class FluidBuilder
{
    private final String namespace;
    private final String baseName;

    private int levelDecrease = 2;
    private int tickRate = 5;
    private boolean isInfinite = false;

    private boolean makeItem = false;
    private boolean makeBlock = false;
    private FluidFactory<?> fluidFactory;
    @Nullable private SoundEvent fillSound = SoundEvents.BUCKET_FILL;
    @Nullable private CreativeModeTab itemGroup = null;

    public FluidBuilder(String namespace, String baseName)
    {
        this.namespace = namespace;
        this.baseName = baseName;
        this.fluidFactory = StandardBuiltFluid::new;
    }

    public FluidBuilder levelDecrease(int levelDecrease)
    {
        this.levelDecrease = levelDecrease;
        return this;
    }

    public FluidBuilder tickRate(int tickRate)
    {
        this.tickRate = tickRate;
        return this;
    }

    public FluidBuilder isInfinite(boolean isInfinite)
    {
        this.isInfinite = isInfinite;
        return this;
    }

    public FluidBuilder fluidFactory(FluidFactory<?> factory)
    {
        this.fluidFactory = factory;
        return this;
    }

    public FluidBuilder withItem(CreativeModeTab itemGroup)
    {
        makeItem = true;
        this.itemGroup = itemGroup;
        return this;
    }

    // I'm not sure what happens if you don't register a block.
    public FluidBuilder withBlock()
    {
        makeBlock = true;
        return this;
    }

//    public FluidBuilder itemGroup(ItemGroup itemGroup)
//    {
//        this.itemGroup = itemGroup;
//        return this;
//    }

    public BuiltFluid<?, ?> build()
    {
        return fluidFactory.create(namespace, baseName, levelDecrease, tickRate, isInfinite, makeBlock, makeItem, fillSound, itemGroup);
    }

    public FluidBuilder fillSound(@Nullable SoundEvent sound)
    {
        this.fillSound = sound;
        return this;
    }

    public interface FluidFactory<T extends BuiltFluid<?, ?>>
    {
        T create(String namespace, String baseName, int levelDecrease, int tickRate, boolean infinite, boolean makeBlock, boolean makeItem, @Nullable SoundEvent fillSound, @Nullable CreativeModeTab itemGroup);
    }
}
