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

package com.neep.neepmeat.network;

import com.neep.neepmeat.NeepMeat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class PlayerImplantStatusS2CPacket
{
    public static final ResourceLocation ID = new ResourceLocation(NeepMeat.NAMESPACE, "player_upgrade_install");

    public static void sendLoad(ServerPlayer player, CompoundTag initialNbt)
    {
        FriendlyByteBuf buf = PacketByteBufs.create();

        buf.writeVarInt(Status.LOAD.ordinal());
        buf.writeNbt(initialNbt);

        ServerPlayNetworking.send(player, ID, buf);
    }

    public static void send(ServerPlayer player, ResourceLocation upgradeId, Status status)
    {
        FriendlyByteBuf buf = PacketByteBufs.create();

        buf.writeVarInt(status.ordinal());
        buf.writeResourceLocation(upgradeId);

        ServerPlayNetworking.send(player, ID, buf);
    }

    @Environment(value= EnvType.CLIENT)
    public static class Client
    {
        public static void registerReceiver()
        {
//            ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) ->
//            {
//                if (client.player == null) return;
//
//                Status status = Status.values()[buf.readVarInt()];
//                switch (status)
//                {
//                    case INSTALL -> PlayerImplantManager.get(client.player).installImplant(buf.readIdentifier());
//                    case REMOVE -> PlayerImplantManager.get(client.player).removeImplant(buf.readIdentifier());
//                    case LOAD ->
//                    {
//                        NbtCompound fullNbt = buf.readNbt();
//                        PlayerImplantManager.get(client.player).sync(fullNbt);
//                    }
//                }
//            });
        }
    }

    public enum Status
    {
        INSTALL,
        REMOVE,
        LOAD;
    }
}
