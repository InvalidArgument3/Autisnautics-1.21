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

package com.neep.neepmeat.network.plc;

import com.neep.meatlib.network.PacketBufUtil;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.client.plc.PLCHudRenderer;
import com.neep.neepmeat.network.S2CSender;
import com.neep.neepmeat.plc.PLCBlocks;
import com.neep.neepmeat.plc.block.entity.PLCBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class PLCRobotEnterS2C
{
    public static final ResourceLocation ID = new ResourceLocation(NeepMeat.NAMESPACE, "plc_robot_enter");

    public static void init()
    {
        ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) ->
        {
            BlockPos pos = PacketBufUtil.readBlockPos(buf);

            server.execute(() ->
            {
                if (player.level().getBlockEntity(pos) instanceof PLCBlockEntity be)
                {
                    be.exit();
                }
            });
        });
    }

    public static void send(Player player, PLCBlockEntity be)
    {
        var buf = S2CSender.getBuf();

        PacketBufUtil.writeBlockPos(buf, be.getBlockPos());

        S2CSender.send(player, ID, buf);
    }

    @Environment(value = EnvType.CLIENT)
    public static class Client
    {
        public static void registerReceiver()
        {
            ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) ->
            {
                BlockPos pos = PacketBufUtil.readBlockPos(buf);
                client.execute(() ->
                {
                    if (client.level != null)
                    {
                        client.level.getBlockEntity(pos, PLCBlocks.PLC_ENTITY).ifPresent(be ->
                        {
                            PLCHudRenderer.enter(be);
                        });
                    }
                });
            });
        }

        public static void send(PLCBlockEntity be)
        {
            FriendlyByteBuf buf = PacketByteBufs.create();

            PacketBufUtil.writeBlockPos(buf, be.getBlockPos());

            ClientPlayNetworking.send(ID, buf);
        }
    }
}
