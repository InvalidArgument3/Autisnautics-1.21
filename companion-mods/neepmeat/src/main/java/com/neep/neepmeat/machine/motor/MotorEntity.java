/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.machine.motor;

import com.neep.neepmeat.api.machine.MotorisedBlock;
import org.jetbrains.annotations.Nullable;

public interface MotorEntity {
    default void onRemoved() {
        MotorisedBlock motorised = getConnectedBlock();
        if (motorised != null) {
            motorised.setInputPower(0);
            motorised.onMotorRemoved();
        }
    }

    float getRotorAngle();

    float getSpeed();

    double getMechPUPower();

    @Nullable
    MotorisedBlock getConnectedBlock();
}
