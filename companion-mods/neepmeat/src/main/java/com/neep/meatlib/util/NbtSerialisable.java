/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.util;

import net.minecraft.nbt.CompoundTag;

/**
 * Block-entity and inventory save data still uses NBT in 1.21. Item extras use data components.
 * The British spelling matches upstream so machine ports can keep the name.
 */
public interface NbtSerialisable {
    CompoundTag writeNbt(CompoundTag nbt);

    void readNbt(CompoundTag nbt);
}
