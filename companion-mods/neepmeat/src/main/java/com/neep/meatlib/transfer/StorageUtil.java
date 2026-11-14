/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */


package com.neep.meatlib.transfer;

import net.minecraft.nbt.CompoundTag;

public final class StorageUtil {
    private StorageUtil() {}
    public static CompoundTag toNbt(Object storage) { return new CompoundTag(); }
    public static void readNbt(Object storage, CompoundTag tag) {}
    public static void writeNbt(Object storage, CompoundTag tag) {}
}
