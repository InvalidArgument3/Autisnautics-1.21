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

package com.neep.neepmeat.api.processing;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class FluidEnegyRegistry extends HashMap<Fluid, FluidEnegyRegistry.Entry>
{
    protected static final FluidEnegyRegistry INSTANCE = new FluidEnegyRegistry();
    public static final com.neep.neepmeat.api.processing.FluidEnegyRegistry.Entry EMPTY = new com.neep.neepmeat.api.processing.FluidEnegyRegistry.Entry(0, false, null);

    public static FluidEnegyRegistry getInstance()
    {
        return INSTANCE;
    }

    /**
     * @param baseEnergy The energy in a droplet of this fluid
     */
    public void register(Fluid fluid, double baseEnergy, boolean isEnergised, @Nullable Fluid exhaustType)
    {
        put(fluid, new FluidEnegyRegistry.Entry(baseEnergy, isEnergised, exhaustType));
    }

    @NotNull
    public com.neep.neepmeat.api.processing.FluidEnegyRegistry.Entry getOrEmpty(Fluid fluid)
    {
        return getOrDefault(fluid, EMPTY);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static record Entry(double baseEnergy, boolean isEnergised, @Nullable Fluid exhaustType)
    {
        public boolean hasExhaust()
        {
            return exhaustType != null;
        }

        public @Nullable FluidVariant getExhaustVariant()
        {
            return hasExhaust() ? FluidVariant.of(exhaustType) : null;
        }
    }
}
