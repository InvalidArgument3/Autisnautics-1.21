/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.api.machine;

/**
 * Fabric's {@code BlockApiLookup} is a NeoForge capability once the motor exists.
 * {@code motorTick} does not take {@code MotorEntity} yet. That class is still upstream.
 */
public interface MotorisedBlock {
    boolean motorTick();

    void setInputPower(float power);

    default void onMotorRemoved() {}

    interface DiagnosticsProvider {
        Diagnostics getDiagnostics();
    }

    record Diagnostics(boolean problem, float power, float minPower) {
        public static Diagnostics insufficientPower(boolean problem, float power, float minPower) {
            return new Diagnostics(problem, power, minPower);
        }
    }
}
