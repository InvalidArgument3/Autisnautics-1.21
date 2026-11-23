/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */


package net.fabricmc.fabric.api.transfer.v1.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class TransferVariant<T> {
    public boolean isBlank() { return true; }
    public CompoundTag toNbt() { return new CompoundTag(); }
    public CompoundTag getNbt() { return null; }
    public void toPacket(FriendlyByteBuf buf) {}
    public static <T> TransferVariant<T> blank() { return new TransferVariant<>(); }
    public static <T> TransferVariant<T> fromNbt(CompoundTag tag) { return blank(); }
}
