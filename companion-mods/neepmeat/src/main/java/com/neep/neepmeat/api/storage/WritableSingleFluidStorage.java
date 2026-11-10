/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Droplet tank. Exposed to pipes as millibuckets, dropping any remainder under 81 droplets.
 */

package com.neep.neepmeat.api.storage;

import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.SingleVariantStorage;
import com.neep.meatlib.transfer.Storage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class WritableSingleFluidStorage extends SingleVariantStorage<FluidVariant> implements IFluidHandler {
    private final long capacity;
    private final Runnable callback;

    public WritableSingleFluidStorage(long capacity, Runnable callback) {
        super(FluidVariant.BLANK);
        this.capacity = capacity;
        this.callback = callback == null ? () -> {} : callback;
    }

    @Override
    protected FluidVariant blank() {
        return FluidVariant.BLANK;
    }

    @Override
    protected long capacity(FluidVariant resource) {
        return capacity;
    }

    @Override
    protected boolean matches(FluidVariant stored, FluidVariant offered) {
        return stored.fluid() == offered.fluid();
    }

    @Override
    protected void onFinalCommit() {
        callback.run();
    }

    public Storage<FluidVariant> asStorage() {
        return this;
    }

    public void writeNbt(CompoundTag tag) {
        tag.putString("fluid", net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(variant.fluid()).toString());
        tag.putLong("amount", amount);
    }

    public void readNbt(CompoundTag tag) {
        amount = tag.getLong("amount");
        net.minecraft.resources.ResourceLocation id = net.minecraft.resources.ResourceLocation.tryParse(tag.getString("fluid"));
        Fluid fluid = id == null ? Fluids.EMPTY : net.minecraft.core.registries.BuiltInRegistries.FLUID.getOptional(id).orElse(Fluids.EMPTY);
        variant = FluidVariant.of(fluid);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        if (amount <= 0 || variant.isBlank()) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(variant.fluid(), (int) (amount / FluidConstants.DROPLETS_PER_MILLIBUCKET));
    }

    @Override
    public int getTankCapacity(int tank) {
        return (int) (capacity / FluidConstants.DROPLETS_PER_MILLIBUCKET);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return !stack.isEmpty();
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }
        long droplets = resource.getAmount() * (long) FluidConstants.DROPLETS_PER_MILLIBUCKET;
        try (var transaction = com.neep.meatlib.transfer.Transaction.openOuter()) {
            long moved = insert(FluidVariant.of(resource.getFluid()), droplets, transaction);
            if (action.execute()) {
                transaction.onRename();
                callback.run();
            }
            return (int) (moved / FluidConstants.DROPLETS_PER_MILLIBUCKET);
        }
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0 || variant.isBlank()) {
            return FluidStack.EMPTY;
        }
        Fluid fluid = variant.fluid();
        long droplets = maxDrain * (long) FluidConstants.DROPLETS_PER_MILLIBUCKET;
        try (var transaction = com.neep.meatlib.transfer.Transaction.openOuter()) {
            long moved = extract(variant, droplets, transaction);
            if (action.execute()) {
                transaction.onRename();
                callback.run();
            }
            int millibuckets = (int) (moved / FluidConstants.DROPLETS_PER_MILLIBUCKET);
            return millibuckets <= 0 ? FluidStack.EMPTY : new FluidStack(fluid, millibuckets);
        }
    }
}
