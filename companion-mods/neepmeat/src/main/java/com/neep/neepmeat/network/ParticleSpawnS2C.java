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
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ParticleSpawnS2C
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "particle_spawn");

    public static <T extends ParticleOptions> void sendNearby(ServerLevel world, BlockPos pos, T particle, Vec3 origin, Vec3 velocity, Vec3 spread, int count)
    {
        for (ServerPlayer player : PlayerLookup.around(world, pos, 32))
        {
            send(player, particle, origin, velocity, spread, count);
        }
    }

    public static <T extends ParticleOptions> void send(ServerPlayer player, T particle, Vec3 origin, Vec3 velocity, Vec3 spread, int count)
    {
        RegistryFriendlyByteBuf payload = new RegistryFriendlyByteBuf(PacketByteBufs.create(), player.registryAccess());
        payload.writeDouble(origin.x);
        payload.writeDouble(origin.y);
        payload.writeDouble(origin.z);
        payload.writeDouble(velocity.x);
        payload.writeDouble(velocity.y);
        payload.writeDouble(velocity.z);
        payload.writeDouble(spread.x);
        payload.writeDouble(spread.y);
        payload.writeDouble(spread.z);
        payload.writeVarInt(count);
        payload.writeById(BuiltInRegistries.PARTICLE_TYPE::getId, particle.getType());
        writeOptions(payload, particle);
        ServerPlayNetworking.send(player, ID, payload);
    }

    @SuppressWarnings("unchecked")
    private static <T extends ParticleOptions> void writeOptions(RegistryFriendlyByteBuf buf, T particle)
    {
        ((ParticleType<T>) particle.getType()).streamCodec().encode(buf, particle);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Client
    {
        public static void registerReceiver()
        {
            ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) ->
            {
                RegistryFriendlyByteBuf payload = buf instanceof RegistryFriendlyByteBuf friendly
                        ? friendly
                        : new RegistryFriendlyByteBuf(buf, client.level.registryAccess());
                Vec3 origin = new Vec3(payload.readDouble(), payload.readDouble(), payload.readDouble());
                Vec3 velocity = new Vec3(payload.readDouble(), payload.readDouble(), payload.readDouble());
                Vec3 spread = new Vec3(payload.readDouble(), payload.readDouble(), payload.readDouble());
                int count = payload.readVarInt();
                ParticleType<?> type = payload.readById(BuiltInRegistries.PARTICLE_TYPE::byId);
                ParticleOptions effect = type == null ? null : readOptions(payload, type);
                client.execute(() ->
                {
                    if (effect == null || client.level == null)
                    {
                        return;
                    }
                    for (int i = 0; i < count; ++i)
                    {
                        double px = origin.x + (Math.random() - 0.5) * 2 * spread.x;
                        double py = origin.y + (Math.random() - 0.5) * 2 * spread.y;
                        double pz = origin.z + (Math.random() - 0.5) * 2 * spread.z;
                        client.level.addParticle(effect, px, py, pz, velocity.x, velocity.y, velocity.z);
                    }
                });
            });
        }

        @SuppressWarnings("unchecked")
        private static <T extends ParticleOptions> T readOptions(RegistryFriendlyByteBuf buf, ParticleType<T> type)
        {
            return type.streamCodec().decode(buf);
        }
    }
}
