/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Amounts are NeoForge millibuckets. Capability calls ignore the Fabric transaction.
 */

package com.neep.meatlib.transfer;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class FluidHandlerStorage implements Storage<FluidVariant> {
    private final IFluidHandler handler;

    private FluidHandlerStorage(IFluidHandler handler) {
        this.handler = handler;
    }

    public static FluidHandlerStorage of(IFluidHandler handler) {
        return new FluidHandlerStorage(handler);
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource == null || resource.isBlank() || maxAmount <= 0) {
            return 0;
        }
        int amount = (int) Math.min(maxAmount, Integer.MAX_VALUE);
        return handler.fill(new FluidStack(resource.fluid(), amount), IFluidHandler.FluidAction.EXECUTE);
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource == null || resource.isBlank() || maxAmount <= 0) {
            return 0;
        }
        int amount = (int) Math.min(maxAmount, Integer.MAX_VALUE);
        FluidStack drained = handler.drain(new FluidStack(resource.fluid(), amount), IFluidHandler.FluidAction.EXECUTE);
        return drained.getAmount();
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        List<StorageView<FluidVariant>> views = new ArrayList<>();
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            int index = tank;
            views.add(new StorageView<>() {
                @Override
                public FluidVariant getResource() {
                    return FluidVariant.of(handler.getFluidInTank(index).getFluid());
                }

                @Override
                public long getAmount() {
                    return handler.getFluidInTank(index).getAmount();
                }

                @Override
                public long getCapacity() {
                    return handler.getTankCapacity(index);
                }
            });
        }
        return views.iterator();
    }
}
