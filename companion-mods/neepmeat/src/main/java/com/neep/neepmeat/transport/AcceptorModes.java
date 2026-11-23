/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port of the upstream pump mode enum.
 */

package com.neep.neepmeat.transport;

public enum AcceptorModes {
    NONE(0),
    PULL(1),
    PUSH(2),
    INSERT_ONLY(3),
    EXTRACT_ONLY(4),
    INSERT_EXTRACT(5),
    SENSOR(6);

    private final int id;

    AcceptorModes(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isDriving() {
        return this == PUSH || this == PULL;
    }

    public static AcceptorModes byId(int id) {
        AcceptorModes[] values = values();
        return values[Math.floorMod(id, values.length)];
    }
}
