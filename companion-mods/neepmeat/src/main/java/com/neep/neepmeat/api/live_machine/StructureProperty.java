/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.api.live_machine;

public enum StructureProperty {
    FAULT_TOLERANCE(0),
    SPEED(1),
    SELF_REPAIR(0),
    MAX_POWER(0),
    MASS(1);

    private final float def;

    StructureProperty(float def) {
        this.def = def;
    }

    public float defaultValue() {
        return def;
    }
}
