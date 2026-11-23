/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.transfer;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * Several {@link FluidTank}s behind one {@link IFluidHandler}. Amounts are milli-buckets,
 * which is NeoForge's unit. Fabric's 81-droplet unit is not used here.
 */
public final class MultiFluidBuffer implements IFluidHandler {
    private final FluidTank[] tanks;

    public MultiFluidBuffer(int tankCount, int capacityPerTank) {
        this.tanks = new FluidTank[tankCount];
        for (int i = 0; i < tankCount; i++) {
            tanks[i] = new FluidTank(capacityPerTank);
        }
    }

    public FluidTank tank(int index) {
        return tanks[index];
    }

    @Override
    public int getTanks() {
        return tanks.length;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return tanks[tank].getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return tanks[tank].getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return tanks[tank].isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }
        int filled = 0;
        FluidStack remaining = resource.copy();
        for (FluidTank tank : tanks) {
            int accepted = tank.fill(remaining, action);
            filled += accepted;
            remaining.shrink(accepted);
            if (remaining.isEmpty()) {
                break;
            }
        }
        return filled;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidStack drained = FluidStack.EMPTY;
        int remaining = resource.getAmount();
        for (FluidTank tank : tanks) {
            if (tank.getFluid().isEmpty() || !FluidStack.isSameFluidSameComponents(tank.getFluid(), resource)) {
                continue;
            }
            FluidStack part = tank.drain(remaining, action);
            if (part.isEmpty()) {
                continue;
            }
            if (drained.isEmpty()) {
                drained = part;
            } else {
                drained.grow(part.getAmount());
            }
            remaining -= part.getAmount();
            if (remaining <= 0) {
                break;
            }
        }
        return drained;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack drained = FluidStack.EMPTY;
        int remaining = maxDrain;
        for (FluidTank tank : tanks) {
            if (tank.isEmpty()) {
                continue;
            }
            if (!drained.isEmpty() && !FluidStack.isSameFluidSameComponents(drained, tank.getFluid())) {
                continue;
            }
            FluidStack part = tank.drain(remaining, action);
            if (part.isEmpty()) {
                continue;
            }
            if (drained.isEmpty()) {
                drained = part;
            } else {
                drained.grow(part.getAmount());
            }
            remaining -= part.getAmount();
            if (remaining <= 0) {
                break;
            }
        }
        return drained;
    }
}
