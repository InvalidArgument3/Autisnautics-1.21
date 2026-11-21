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

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMSounds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class PLCErrorMessageS2C
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "plc_error");

    public static void send(ServerPlayer player, String message)
    {
        FriendlyByteBuf buf = PacketByteBufs.create();

        buf.writeUtf(message);

        ServerPlayNetworking.send(player, ID, buf);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Client
    {
        public static void init()
        {
            ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) ->
            {
                if (client.level == null || client.player == null)
                    return;

                String message = buf.readUtf();

                client.execute(() ->
                {
                    client.player.sendSystemMessage(Component.nullToEmpty(message));
                    client.getSoundManager().play(SimpleSoundInstance.forUI(NMSounds.ERROR.get(), 1.0f));
                });
            });
        }
    }
}
