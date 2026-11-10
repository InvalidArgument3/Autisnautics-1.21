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
import com.neep.neepmeat.item.util.TransformingTools;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class ToolTransformPacket
{
    public static final ResourceLocation ID = new ResourceLocation(NeepMeat.NAMESPACE, "key_press");

    public static void registerReceiver()
    {
        ServerPlayNetworking.registerGlobalReceiver(ID, ((server, player, handler, buf, responseSender) ->
        {
            ItemStack oldStack = player.getItemInHand(InteractionHand.MAIN_HAND);
            TransformingTools.swap(oldStack, player);
        }));
    }

    @Environment(value=EnvType.CLIENT)
    public static class Client
    {
        public static void send()
        {
            FriendlyByteBuf buf = PacketByteBufs.create();

            ClientPlayNetworking.send(ID, buf);
        }
    }
}
