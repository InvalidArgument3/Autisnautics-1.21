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
    /** A blank variant has no object. Item and fluid variants return their real value. */
    TransferVariant<?> BLANK = new TransferVariant<Object>() {
        @Override
        public boolean isBlank() {
            return true;
        }

        @Override
        public Object getObject() {
            return null;
        }
    };

    boolean isBlank();

    T getObject();

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
