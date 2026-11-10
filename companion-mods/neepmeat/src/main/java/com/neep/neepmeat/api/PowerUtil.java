/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.api;

import com.neep.meatlib.MeatLib;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.processing.FluidEnegyRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.material.Fluid;

import java.text.DecimalFormat;

public final class PowerUtil {
    /** Unloaded-motor friction. Avoids a divide by zero when the rotor speed is calculated. */
    public static final float MOTOR_TORQUE_LOSS = 300f;
    /** One per-unit of energy is one step of a recipe counter. */
    public static final long BASE_POWER = 1000;
    public static final long DROPLET_POWER = 1;
    /** Fabric droplets in one NeoForge millibucket. Energy is stored per millibucket. */
    public static final int DROPLETS_PER_MILLIBUCKET = 81;

    public static final Component POWER_UNIT = Component.translatable("message." + NeepMeat.ID + ".power_unit");
    public static final Component POWER = Component.translatable("message." + NeepMeat.ID + ".power");
    public static final DecimalFormat POWER_FORMAT = new DecimalFormat("###.##");

    private PowerUtil() {}

    public static double perUnitToAbsolute(double perUnit) {
        return Math.round(perUnit * BASE_POWER);
    }

    public static double absoluteToPerUnit(long absolute) {
        return (double) absolute / BASE_POWER;
    }

    public static double perUnitToAbsWatt(double perUnit) {
        return perUnitToAbsolute(perUnit) * 20;
    }

    public static MutableComponent perUnitToText(double perUnit) {
        return Component.literal(POWER_FORMAT.format(perUnitToAbsolute(perUnit))).append(POWER_UNIT);
    }

    public static String formatEnergy(long energy, int decimals) {
        return MeatLib.formatEngineering(energy, decimals, true, "eJ");
    }

    public static long absToAmount(Fluid fluid, long energy) {
        if (energy == 0) {
            return 0;
        }
        double base = FluidEnegyRegistry.getInstance().getOrEmpty(fluid).baseEnergy();
        if (base == 0) {
            return 0;
        }
        return (long) Math.floor(energy / base);
    }

    public static long amountToAbsEnergy(long amount, Fluid fluid) {
        return (long) (FluidEnegyRegistry.getInstance().getOrEmpty(fluid).baseEnergy() * amount);
    }
}
