/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.api.live_machine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public class DegradationManager {
    private final RateFunction degradationRate;
    private double degradation;

    public DegradationManager(RateFunction degradationRate) {
        this.degradationRate = degradationRate;
    }

    public float getDegradation() {
        return (float) degradation;
    }

    public void setDegradation(double degradation) {
        this.degradation = Mth.clamp(degradation, 0, 1);
    }

    public long estimateRul() {
        double rate = degradationRate.get(degradation);
        if (rate <= 0) {
            return -1;
        }
        return (long) ((1 - degradation) / rate);
    }

    public void tick() {
        degradation = Mth.clamp(degradation + degradationRate.get(degradation), 0, 1);
    }

    public void write(CompoundTag tag) {
        tag.putDouble("degradation", degradation);
    }

    public void read(CompoundTag tag) {
        degradation = tag.getDouble("degradation");
    }

    @FunctionalInterface
    public interface RateFunction {
        double get(double degradation);
    }
}
