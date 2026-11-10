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
import com.neep.meatlib.util.MiscUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import java.text.DecimalFormat;

public class TankMessagePacket
{
    public static final ResourceLocation TANK_MESSAGE = new ResourceLocation(NeepMeat.NAMESPACE, "tank_message");

    public static void send(ServerPlayer player, BlockPos pos, long amount, FluidVariant resource)
    {
        FriendlyByteBuf buf = PacketByteBufs.create();

        buf.writeBlockPos(pos);
        buf.writeLong(amount);
        buf.writeNbt(resource.toNbt());

        ServerPlayNetworking.send(player, TANK_MESSAGE, buf);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Client
    {
        public static void registerReceiver()
        {
            ClientPlayNetworking.registerGlobalReceiver(TANK_MESSAGE, (client, handler, buf, responseSender) ->
            {
                BlockPos pos = buf.readBlockPos();
                long amount = buf.readLong();
                FluidVariant resource = FluidVariant.fromNbt(buf.readNbt());
                MutableComponent text = FluidVariantAttributes.getName(resource).copy();

//                long buckets = amount / FluidConstants.BUCKET;
//                long remainder = amount % FluidConstants.BUCKET;
//                double dRemainder = remainder / FluidConstants.BUCKET;

                double buckets = (double) amount / FluidConstants.BUCKET;

                DecimalFormat df = new DecimalFormat("###.###");
                if (buckets > 10)
                {
                    client.player.displayClientMessage(resource.isBlank() ? Component.nullToEmpty("Empty") : text.append(": " +  df.format(buckets) + "b"), true);
                }
                else
                {
                    client.player.displayClientMessage(resource.isBlank() ? Component.nullToEmpty("Empty") : text.append(": " + MiscUtil.dropletsToMb(amount) + "mb"), true);
                }

            });
        }
    }
}
