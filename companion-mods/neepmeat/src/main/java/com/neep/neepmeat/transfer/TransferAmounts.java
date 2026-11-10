/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric droplets become NeoForge millibuckets by dividing by 81 and dropping the remainder.
 * One millibucket inserted through an IFluidHandler becomes 81 droplets.
 */

package com.neep.neepmeat.transfer;

import com.neep.neepmeat.api.PowerUtil;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

public final class TransferAmounts {
    private TransferAmounts() {}

    public static long dropletsToMillibuckets(long droplets) {
        return droplets / PowerUtil.DROPLETS_PER_MILLIBUCKET;
    }

    public static long millibucketsToDroplets(long millibuckets) {
        return millibuckets * PowerUtil.DROPLETS_PER_MILLIBUCKET;
    }

    public static long leftoverDroplets(long droplets) {
        return droplets % PowerUtil.DROPLETS_PER_MILLIBUCKET;
    }

    public static FluidStack executeFill(IFluidHandler handler, FluidStack stack) {
        int filled = handler.fill(stack, IFluidHandler.FluidAction.EXECUTE);
        return new FluidStack(stack.getFluid(), filled);
    }

    public static FluidStack simulateFill(IFluidHandler handler, FluidStack stack) {
        int filled = handler.fill(stack, IFluidHandler.FluidAction.SIMULATE);
        return new FluidStack(stack.getFluid(), filled);
    }

    public static net.minecraft.world.item.ItemStack simulateExtract(IItemHandler handler, int slot, int amount) {
        return handler.extractItem(slot, amount, true);
    }

    public static net.minecraft.world.item.ItemStack executeExtract(IItemHandler handler, int slot, int amount) {
        return handler.extractItem(slot, amount, false);
    }
}
