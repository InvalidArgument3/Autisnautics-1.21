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
import com.neep.neepmeat.entity.AnimationSyncable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class EntityAnimationS2C
{
    public static final ResourceLocation ID = new ResourceLocation(NeepMeat.NAMESPACE, "entity_animation");

    public static void send(ServerPlayer player, Entity entity, String animation)
    {
        FriendlyByteBuf buf = PacketByteBufs.create();

        buf.writeVarInt(entity.getId());
        buf.writeUtf(animation);

        ServerPlayNetworking.send(player, ID, buf);
    }

    @Environment(value= EnvType.CLIENT)
    public static class Client
    {
        public static void registerReceiver()
        {
            ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) ->
            {
                if (client.level == null)
                    return;

                int entityId = buf.readVarInt();
                Entity entity = client.level.getEntity(entityId);
                String name = buf.readUtf();

                if (entity instanceof AnimationSyncable animationSyncable)
                {
                    animationSyncable.getQueue().add(name);
                }
            });
        }
    }
}
