/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Replies go out on the neepmeat:raw payload.
 */

package net.fabricmc.fabric.api.networking.v1;

import com.neep.neepmeat.network.RawPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class PacketSender {
    public void sendPacket(ResourceLocation id, FriendlyByteBuf buf) {
        PacketDistributor.sendToAllPlayers(RawPayload.of(id, buf));
    }

    public void sendPacket(ServerPlayer player, ResourceLocation id, FriendlyByteBuf buf) {
        PacketDistributor.sendToPlayer(player, RawPayload.of(id, buf));
    }
}
