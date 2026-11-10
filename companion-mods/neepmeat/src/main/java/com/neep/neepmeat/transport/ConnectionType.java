/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.transport;

import net.minecraft.util.StringRepresentable;

/** Same names as upstream: the blockstate values are the strings "true" and "false". */
public enum ConnectionType implements StringRepresentable {
    SIDE("true"),
    NONE("false");

    private final String name;

    ConnectionType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public boolean connected() {
        return this == SIDE;
    }
}
