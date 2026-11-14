/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */


package net.fabricmc.fabric.api.transfer.v1.fluid;

import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidVariant extends TransferVariant<Fluid> {
    public static final FluidVariant BLANK = new FluidVariant();
    public static FluidVariant of(Fluid fluid) { return fluid == null || fluid == Fluids.EMPTY ? BLANK : new FluidVariant(); }
    public static FluidVariant fromNbt(CompoundTag tag) { return BLANK; }
    public Fluid getFluid() { return Fluids.EMPTY; }
    @Override public boolean isBlank() { return true; }
}
