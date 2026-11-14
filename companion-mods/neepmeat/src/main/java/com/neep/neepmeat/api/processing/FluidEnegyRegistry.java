/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * The class name keeps upstream's spelling.
 */

package com.neep.neepmeat.api.processing;

import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class FluidEnegyRegistry extends HashMap<Fluid, FluidEnegyRegistry.Entry> {
    private static final FluidEnegyRegistry INSTANCE = new FluidEnegyRegistry();
    public static final Entry EMPTY = new Entry(0, false, null);

    public static FluidEnegyRegistry getInstance() {
        return INSTANCE;
    }

    /** {@code baseEnergy} is energy per Fabric droplet, matching upstream. */
    public void register(Fluid fluid, double baseEnergy, boolean energised, @Nullable Fluid exhaust) {
        put(fluid, new Entry(baseEnergy, energised, exhaust));
    }

    public @NotNull Entry getOrEmpty(Fluid fluid) {
        return getOrDefault(fluid, EMPTY);
    }

    public record Entry(double baseEnergy, boolean energised, @Nullable Fluid exhaust) {
        public boolean hasExhaust() {
            return exhaust != null;
        }
    }
}
