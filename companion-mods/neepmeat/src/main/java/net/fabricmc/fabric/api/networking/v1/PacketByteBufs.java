/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric's PacketByteBufs, backed by a NeoForge buffer.
 */

package net.fabricmc.fabric.api.networking.v1;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

public final class PacketByteBufs {
    private PacketByteBufs() {}

    public static FriendlyByteBuf create() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }
}
