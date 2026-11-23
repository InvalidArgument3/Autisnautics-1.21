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
import com.neep.neepmeat.machine.pylon.PylonBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class BlockSoundPacket
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block_sound");

    public static void send(ServerLevel world, BlockPos pos)
    {
        FriendlyByteBuf buf = PacketByteBufs.create();

        buf.writeBlockPos(pos);
//        buf.writeLong(amount);
//        buf.writeNbt(resource.toNbt());

        world.players().forEach(p -> ServerPlayNetworking.send(p, ID, buf));
    }

    @Environment(value=Dist.CLIENT)
    public static class Client
    {
        public static void registerReceiver()
        {
            ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) ->
            {
                BlockPos pos = buf.readBlockPos();
                Level world = Minecraft.getInstance().level;
                if (world.getBlockEntity(pos) instanceof PylonBlockEntity be)
                {
//                    MinecraftClient.getInstance().getSoundManager().play(new PylonSoundInstance(be, pos, NMSounds.AIRTRUCK_RUNNING.get(), SoundCategory.BLOCKS));
                }
            });
        }
    }
}
