/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric Fluid.still() and Fluid.flowing() on a flowing fluid.
 */

package com.neep.neepmeat.fluid;

import com.neep.meatlib.fluid.BuiltFluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

public final class FluidForms {
    private FluidForms() {}

    public static Fluid still(BuiltFluid<?, ?> built) {
        return built == null ? null : built.still();
    }

    public static Fluid flowing(BuiltFluid<?, ?> built) {
        return built == null ? null : built.flowing();
    }

    public static Fluid still(Fluid fluid) {
        return fluid instanceof FlowingFluid flowing ? flowing.getSource() : fluid;
    }

    public static Fluid flowing(Fluid fluid) {
        return fluid instanceof FlowingFluid flowing ? flowing.getFlowing() : fluid;
    }
}
