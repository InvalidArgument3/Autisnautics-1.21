/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Cardinal Components call sites stay. Storage is a NeoForge attachment.
 */

package dev.onyxstudios.cca.api.v3.component;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public interface Component {
    void readFromNbt(@NotNull CompoundTag tag);

    void writeToNbt(@NotNull CompoundTag tag);
}
