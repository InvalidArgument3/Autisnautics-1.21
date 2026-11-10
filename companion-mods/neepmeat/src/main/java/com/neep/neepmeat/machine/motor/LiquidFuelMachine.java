/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fluid amounts are millibuckets. Upstream counted Fabric droplets.
 */

package com.neep.neepmeat.machine.motor;

import com.neep.neepmeat.api.processing.FluidEnegyRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class LiquidFuelMachine {
    private final FluidTank tank;

    public LiquidFuelMachine(int capacity) {
        this.tank = new FluidTank(capacity, stack ->
                FluidEnegyRegistry.getInstance().getOrEmpty(stack.getFluid()).baseEnergy() > 0);
    }

    public FluidTank tank() {
        return tank;
    }

    /**
     * Burns enough fuel to approach {@code absolute} energy and returns the energy actually obtained.
     * The result is truncated the same way as upstream: amount is an integer, then multiplied back.
     */
    public long extractEnergy(long absolute) {
        FluidStack stored = tank.getFluid();
        if (stored.isEmpty() || absolute <= 0) {
            return 0;
        }
        double baseEnergy = FluidEnegyRegistry.getInstance().getOrEmpty(stored.getFluid()).baseEnergy();
        if (baseEnergy <= 0) {
            return 0;
        }
        int extractAmount = (int) (absolute / baseEnergy);
        if (extractAmount <= 0) {
            return 0;
        }
        FluidStack drained = tank.drain(extractAmount, IFluidHandler.FluidAction.EXECUTE);
        return (long) (drained.getAmount() * baseEnergy);
    }

    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("fuel", tank.writeToNBT(registries, new CompoundTag()));
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("fuel")) {
            tank.readFromNBT(registries, tag.getCompound("fuel"));
        }
    }
}
