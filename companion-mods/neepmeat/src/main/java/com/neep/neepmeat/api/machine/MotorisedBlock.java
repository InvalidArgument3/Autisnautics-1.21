/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.api.machine;

/**
 * The motor in front calls {@link #motorTick} and {@link #setInputPower}.
 */
public interface MotorisedBlock {
    boolean motorTick(com.neep.neepmeat.machine.motor.MotorEntity motor);

    void setInputPower(float power);

    default void onMotorRemoved() {}

    default float getLoadTorque() {
        return com.neep.neepmeat.api.PowerUtil.MOTOR_TORQUE_LOSS;
    }

    interface DiagnosticsProvider {
        Diagnostics getDiagnostics();
    }

    record Diagnostics(boolean problem, float power, float minPower) {
        public static Diagnostics insufficientPower(boolean problem, float power, float minPower) {
            return new Diagnostics(problem, power, minPower);
        }
    }
}
