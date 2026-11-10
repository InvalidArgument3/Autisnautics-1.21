/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.transfer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public interface TransferVariant<T> {
    TransferVariant<?> BLANK = () -> true;

    boolean isBlank();

    default CompoundTag toNbt() {
        return new CompoundTag();
    }

    default CompoundTag getNbt() {
        return null;
    }

    default void toPacket(FriendlyByteBuf buf) {}

    @SuppressWarnings("unchecked")
    static <T> TransferVariant<T> blank() {
        return (TransferVariant<T>) BLANK;
    }
}
