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
import com.neep.neepmeat.plc.block.entity.PLCBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;

public class PLCRobotC2S
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "robot_update");

    public static void send(PLCBlockEntity be, ServerLevel world)
    {
        FriendlyByteBuf buf = PacketByteBufs.create();

        PacketBufUtil.writeBlockPos(buf, be.getBlockPos());
        buf.writeDouble(be.getSurgeryRobot().getX());
        buf.writeDouble(be.getSurgeryRobot().getY());
        buf.writeDouble(be.getSurgeryRobot().getZ());

        buf.writeFloat(be.getSurgeryRobot().getPitch());
        buf.writeFloat(be.getSurgeryRobot().getYaw());

        PlayerLookup.around(world, be.getBlockPos(), 30).forEach(e ->
        {
            ServerPlayNetworking.send(e, ID, buf);
        });
    }

    public static void init()
    {
        ServerPlayNetworking.registerGlobalReceiver(ID, PLCRobotC2S::apply);
    }

    private static void apply(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender sender)
    {
        BlockPos pos = PacketBufUtil.readBlockPos(buf);
        Level world = player.level();
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();

        float pitch = buf.readFloat();
        float yaw = buf.readFloat();

        server.execute(() ->
        {
            if (world.getBlockEntity(pos) instanceof PLCBlockEntity be)
            {
                be.getSurgeryRobot().setPos(x, y, z);
                be.getSurgeryRobot().setPitchYaw(pitch, yaw);
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public static class Client
    {
        public static void send(PLCBlockEntity be)
        {
            FriendlyByteBuf buf = PacketByteBufs.create();

            PacketBufUtil.writeBlockPos(buf, be.getBlockPos());
            buf.writeDouble(be.getSurgeryRobot().getX());
            buf.writeDouble(be.getSurgeryRobot().getY());
            buf.writeDouble(be.getSurgeryRobot().getZ());

            buf.writeFloat(be.getSurgeryRobot().getPitch());
            buf.writeFloat(be.getSurgeryRobot().getYaw());

            ClientPlayNetworking.send(ID, buf);
        }

        public static void init()
        {
            ClientPlayNetworking.registerGlobalReceiver(ID, Client::apply);
        }

        private static void apply(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender sender)
        {
            BlockPos pos = PacketBufUtil.readBlockPos(buf);
            Level world = client.level;

            if (world == null)
                return;

            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();

            float pitch = buf.readFloat();
            float yaw = buf.readFloat();

            client.execute(() ->
            {
                if (world.getBlockEntity(pos) instanceof PLCBlockEntity be)
                {
                    be.getSurgeryRobot().setPos(x, y, z);
                    be.getSurgeryRobot().setPitchYaw(pitch, yaw);
                }
            });
        }
    }
}
