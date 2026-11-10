/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.api.plc;

/**
 * The PLC block implements this once that machine is ported. Actuators take it so a
 * program can be told about a failure. Until then the arm is driven directly.
 */
public interface PLC {
    default void raiseError(Error error) {}

    record Error(String message) {}
}
