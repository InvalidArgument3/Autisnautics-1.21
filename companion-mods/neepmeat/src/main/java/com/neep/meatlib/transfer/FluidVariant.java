/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.transfer;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public record FluidVariant(Fluid fluid) {
    public static final FluidVariant BLANK = new FluidVariant(Fluids.EMPTY);

    public static FluidVariant of(Fluid fluid) {
        return fluid == null || fluid == Fluids.EMPTY ? BLANK : new FluidVariant(fluid);
    }

    public boolean isBlank() {
        return fluid == null || fluid == Fluids.EMPTY;
    }

    public boolean isOf(Fluid other) {
        return fluid == other;
    }

    public Fluid getFluid() {
        return fluid;
    }
}
