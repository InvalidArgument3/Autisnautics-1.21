/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.transfer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

public final class FluidVariantAttributes {
    private FluidVariantAttributes() {}

    public static int getColor(FluidVariant variant) {
        return 0xFFFFFF;
    }

    public static Component getName(FluidVariant variant) {
        Fluid fluid = variant == null ? null : variant.getFluid();
        return fluid == null ? Component.empty() : fluid.getFluidType().getDescription();
    }

    public static int getTemperature(FluidVariant variant) {
        if (variant == null || variant.isBlank() || variant.getFluid() == null) {
            return 0;
        }
        return variant.getFluid().getFluidType().getTemperature();
    }

    public static int getLuminance(FluidVariant variant) {
        return 0;
    }

    public static boolean isLighterThanAir(FluidVariant variant) {
        return false;
    }
}
