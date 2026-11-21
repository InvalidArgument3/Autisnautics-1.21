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

import com.neep.meatlib.network.PacketBufUtil;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.neepmeat.client.screen.MachineHudOverlay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
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
import org.jetbrains.annotations.NotNull;

public class MachineDiagnosticsRequest
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "machine_diagnostics");

    public static void registerReceiver()
    {
        ServerPlayNetworking.registerGlobalReceiver(ID, MachineDiagnosticsRequest::applyServer);
    }

    private static void applyServer(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender sender)
    {
        ResourceLocation worldId = ResourceLocation.tryParse(buf.readUtf());
        ServerLevel world = (ServerLevel) handler.player.level();

        // Not sure how this might happen
        if (!world.dimension().location().equals(worldId))
            return;

//        ServerWorld world = server.getWorld(RegistryKey.of(RegistryKeys.DIME, worldId));
        BlockPos pos = PacketBufUtil.readBlockPos(buf);

        server.execute(() ->
        {
            MotorisedBlock.DiagnosticsProvider diagnosticsProvider = MotorisedBlock.DiagnosticsProvider.LOOKUP.find(world, pos, null);
            if (diagnosticsProvider != null)
            {
                send(player, diagnosticsProvider);
            }
        });
    }

    protected static void send(ServerPlayer player, @NotNull MotorisedBlock.DiagnosticsProvider diagnosticsProvider)
    {
        MotorisedBlock.Diagnostics diagnostics = diagnosticsProvider.getDiagnostics();
        if (diagnostics != null)
        {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeNbt(diagnostics.nbt());

            ServerPlayNetworking.send(player, ID, buf);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Client
    {
        public static void registerReceiver()
        {
            ClientPlayNetworking.registerGlobalReceiver(ID, Client::applyClient);
        }

        private static void applyClient(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender packet)
        {
            MotorisedBlock.Diagnostics diagnostics = new MotorisedBlock.Diagnostics(buf.readNbt());
            client.execute(() ->
            {
                MachineHudOverlay.getInstance().receive(diagnostics);
            });
        }

        public static void send(Level world, BlockPos pos)
        {
            if (!world.isClientSide())
                throw new IllegalStateException();

            FriendlyByteBuf buf = PacketByteBufs.create();

            buf.writeUtf(world.dimension().location().toString());
            PacketBufUtil.writeBlockPos(buf, pos);

            ClientPlayNetworking.send(ID, buf);
        }
    }
}
