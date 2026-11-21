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
    /** One block of fluid, the same droplet count as a bucket. */
    public static final long BLOCK = BUCKET;
    public static final long BOTTLE = 27000;
    public static final long INGOT = 9000;
    public static final long NUGGET = 1000;
    public static final int DROPLETS_PER_MILLIBUCKET = 81;
    /** Vanilla lava fluid-type temperature. Hot fluids at or above this undo copper oxidation. */
    public static final int LAVA_TEMPERATURE = 1300;

    private FluidConstants() {}
}
