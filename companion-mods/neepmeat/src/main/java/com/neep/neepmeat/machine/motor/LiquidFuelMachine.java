/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * The buffer is stored in Fabric droplets. Upstream capacity is maxPower / droplet energy,
 * which is 200 droplets for the basic motor. NeoForge moves whole millibuckets, and one
 * millibucket is 81 droplets, so a bucket fill can only store 162 of those 200.
 */

package com.neep.neepmeat.machine.motor;

import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.api.processing.FluidEnegyRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class LiquidFuelMachine implements IFluidHandler {
    private final long capacityDroplets;
    private Fluid fluid = Fluids.EMPTY;
    private long droplets;

    public LiquidFuelMachine(long capacityDroplets) {
        this.capacityDroplets = capacityDroplets;
    }

    public long droplets() {
        return droplets;
    }

    public long capacityDroplets() {
        return capacityDroplets;
    }

    public void storeDroplets(Fluid fluid, long amount) {
        if (amount <= 0 || FluidEnegyRegistry.getInstance().getOrEmpty(fluid).baseEnergy() <= 0) {
            return;
        }
        this.fluid = fluid;
        this.droplets = Math.min(capacityDroplets, amount);
    }

    /**
     * Burns enough droplets to approach {@code absolute} energy. The amount is truncated, then multiplied back.
     */
    public long extractEnergy(long absolute) {
        if (droplets <= 0 || fluid == Fluids.EMPTY || absolute <= 0) {
            return 0;
        }
        double perDroplet = FluidEnegyRegistry.getInstance().getOrEmpty(fluid).baseEnergy();
        if (perDroplet <= 0) {
            return 0;
        }
        long extractAmount = (long) (absolute / perDroplet);
        if (extractAmount <= 0) {
            return 0;
        }
        long extracted = Math.min(droplets, extractAmount);
        droplets -= extracted;
        if (droplets == 0) {
            fluid = Fluids.EMPTY;
        }
        return (long) (extracted * perDroplet);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        if (droplets <= 0 || fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, (int) (droplets / PowerUtil.DROPLETS_PER_MILLIBUCKET));
    }

    @Override
    public int getTankCapacity(int tank) {
        return (int) (capacityDroplets / PowerUtil.DROPLETS_PER_MILLIBUCKET);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return FluidEnegyRegistry.getInstance().getOrEmpty(stack.getFluid()).baseEnergy() > 0;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !isFluidValid(0, resource)) {
            return 0;
        }
        if (droplets > 0 && fluid != resource.getFluid()) {
            return 0;
        }
        long room = (capacityDroplets - droplets) / PowerUtil.DROPLETS_PER_MILLIBUCKET;
        int moved = (int) Math.min(resource.getAmount(), room);
        if (moved > 0 && action.execute()) {
            fluid = resource.getFluid();
            droplets += (long) moved * PowerUtil.DROPLETS_PER_MILLIBUCKET;
        }
        return moved;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || resource.getFluid() != fluid) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        int stored = (int) (droplets / PowerUtil.DROPLETS_PER_MILLIBUCKET);
        int moved = Math.min(maxDrain, stored);
        if (moved <= 0 || fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        Fluid drained = fluid;
        if (action.execute()) {
            droplets -= (long) moved * PowerUtil.DROPLETS_PER_MILLIBUCKET;
            if (droplets == 0) {
                fluid = Fluids.EMPTY;
            }
        }
        return new FluidStack(drained, moved);
    }

    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putString("fuel_id", BuiltInRegistries.FLUID.getKey(fluid).toString());
        tag.putLong("droplets", droplets);
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        droplets = tag.getLong("droplets");
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("fuel_id"));
        fluid = id == null ? Fluids.EMPTY : BuiltInRegistries.FLUID.getOptional(id).orElse(Fluids.EMPTY);
        if (droplets <= 0) {
            fluid = Fluids.EMPTY;
        }
    }

    public @Nullable Fluid fluid() {
        return fluid;
    }
}
