/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.meatlib.network;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.blockentity.BlockEntityClientSerializable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockEntitySync
{
    private static final ResourceLocation CHANNEL_ID = ResourceLocation.fromNamespaceAndPath(MeatLib.NAMESPACE, "block_entity_sync");

    public static <T extends BlockEntity> void send(List<ServerPlayer> players, T be)
    {
        FriendlyByteBuf buf = PacketByteBufs.create();

        CompoundTag nbt = ((BlockEntityClientSerializable) be).createSyncNbt();

        buf.writeBlockPos(be.getBlockPos());
        buf.writeId(BuiltInRegistries.BLOCK_ENTITY_TYPE, be.getType());
        buf.writeNbt(nbt);

        players.forEach(p -> ServerPlayNetworking.send(p, CHANNEL_ID, buf));
    }

    @OnlyIn(Dist.CLIENT)
    public static class Client
    {
        public static void init()
        {
            ClientPlayNetworking.registerGlobalReceiver(CHANNEL_ID, Client::receive);
        }

        private static void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender)
        {
            BlockPos blockPos = buf.readBlockPos();
            BlockEntityType<?> type = buf.readById(BuiltInRegistries.BLOCK_ENTITY_TYPE);
            @Nullable CompoundTag nbt = buf.readNbt();

            client.execute(() ->
            {
                client.level.getBlockEntity(blockPos, type).ifPresent(blockEntity ->
                {
                    if (nbt != null && blockEntity instanceof BlockEntityClientSerializable becs)
                    {
                        becs.fromClientTag(nbt);
                        becs.onReceiveNbt(nbt);
                    }
                });
            });
        }
    }
}