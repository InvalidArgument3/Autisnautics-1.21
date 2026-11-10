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

package com.neep.neepmeat.api.live_machine;

import com.neep.meatlib.util.NbtSerialisable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class DegradationManager implements NbtSerialisable
{
    private final RateFunction degradationRate;
    private final Runnable zeroCross;
    private double degradation = 0f;

    public DegradationManager(RateFunction degradationRate, RandomSource random, Runnable zeroCross)
    {
        this.degradationRate = degradationRate;
        this.zeroCross = zeroCross;
    }

    public float getDegradation()
    {
        return (float) degradation;
    }

    public long estimateRul()
    {
        double rate = degradationRate.get(degradation);
        if (rate <= 0 )
            return -1;

        return (long) ((1 - degradation) / rate);
    }

    public void tick()
    {
        double rate = degradationRate.get(degradation);
        degradation = Mth.clamp(
                degradation + rate,
                0, 1);
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.putDouble("degradation", degradation);
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        this.degradation = nbt.getFloat("degradation");
    }

    public void subtract(float amount)
    {
        degradation = Mth.clamp(degradation - amount, 0, 1);
    }

    @FunctionalInterface
    public interface RateFunction
    {
        double get(double degradation);
    }
}
