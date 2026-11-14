/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Amounts are Fabric droplets, 81 per NeoForge millibucket. A remainder below 81 droplets
 * moves nothing. The handler changes only when the outer transaction commits.
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
        if (!(transaction instanceof Transaction open) || resource == null || resource.isBlank() || maxAmount <= 0) {
            return 0;
        }
        int millibuckets = millibuckets(maxAmount);
        if (millibuckets <= 0) {
            return 0;
        }
        int accepted = handler.fill(new FluidStack(resource.fluid(), millibuckets), IFluidHandler.FluidAction.SIMULATE);
        accepted = Math.min(accepted, millibuckets - pendingMillibuckets(open, resource));
        if (accepted <= 0) {
            return 0;
        }
        int filled = accepted;
        open.reserve(handler, 0, resource, filled);
        open.addOuterCommit(() -> handler.fill(new FluidStack(resource.fluid(), filled), IFluidHandler.FluidAction.EXECUTE));
        return (long) filled * FluidConstants.DROPLETS_PER_MILLIBUCKET;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (!(transaction instanceof Transaction open) || resource == null || resource.isBlank() || maxAmount <= 0) {
            return 0;
        }
        int millibuckets = millibuckets(maxAmount);
        if (millibuckets <= 0) {
            return 0;
        }
        FluidStack drained = handler.drain(new FluidStack(resource.fluid(), millibuckets), IFluidHandler.FluidAction.SIMULATE);
        int accepted = Math.min(drained.getAmount(), millibuckets + pendingMillibuckets(open, resource));
        if (accepted <= 0) {
            return 0;
        }
        int taken = accepted;
        open.reserve(handler, 0, resource, -taken);
        open.addOuterCommit(() -> handler.drain(new FluidStack(resource.fluid(), taken), IFluidHandler.FluidAction.EXECUTE));
        return (long) taken * FluidConstants.DROPLETS_PER_MILLIBUCKET;
    }

    private static int millibuckets(long droplets) {
        long whole = Math.floorDiv(droplets, FluidConstants.DROPLETS_PER_MILLIBUCKET);
        return (int) Math.min(Math.max(whole, 0), Integer.MAX_VALUE);
    }

    private int pendingMillibuckets(Transaction transaction, FluidVariant resource) {
        Object variant = transaction.slotVariant(handler, 0);
        if (variant != null && !resource.equals(variant) && transaction.slotDelta(handler, 0) > 0) {
            return Integer.MAX_VALUE;
        }
        return Math.max(0, transaction.slotDelta(handler, 0));
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
                    return (long) handler.getFluidInTank(index).getAmount() * FluidConstants.DROPLETS_PER_MILLIBUCKET;
                }

                @Override
                public long getCapacity() {
                    return (long) handler.getTankCapacity(index) * FluidConstants.DROPLETS_PER_MILLIBUCKET;
                }
            });
        }
        return views.iterator();
    }
}
