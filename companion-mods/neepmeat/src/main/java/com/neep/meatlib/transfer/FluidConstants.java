/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 * Fabric droplet counts. One NeoForge millibucket is {@code BUCKET / 1000} = 81 droplets.
 */

package com.neep.meatlib.transfer;

public final class FluidConstants {
    public static final long BUCKET = 81000;
    public static final long BOTTLE = 27000;
    public static final long INGOT = 9000;
    public static final long NUGGET = 1000;
    public static final int DROPLETS_PER_MILLIBUCKET = 81;

    private FluidConstants() {}
}
