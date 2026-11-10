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

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.ParamCodecs;
import com.neep.meatlib.network.GlobalChannelManager;
import com.neep.neepmeat.NeepMeat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class ParticleSpawnS2C
{
    public static final GlobalChannelManager<SpawnParticle> CHANNEL = GlobalChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "particle_spawn"),
            ChannelFormat.builder(SpawnParticle.class)
                    .param(ParamCodecs.VEC3D)
                    .param(ParamCodecs.VEC3D)
                    .param(ParamCodecs.VEC3D)
                    .param(ParamCodec.INT)
                    .param(ParamCodecs.registryValue(ParamCodecs.as(ParticleType.class), BuiltInRegistries.PARTICLE_TYPE))
                    .param(ParamCodec.BUF)
                    .build());

    public static <T extends ParticleOptions> void sendNearby(ServerLevel world, BlockPos pos, T particleType, Vec3 origin, Vec3 velocity, Vec3 spread, int count)
    {
        for (ServerPlayer player : PlayerLookup.around(world, pos, 32))
        {
            send(player, particleType, origin, velocity, spread, count);
        }
    }

    public static <T extends ParticleOptions> void send(ServerPlayer player, T particle, Vec3 origin, Vec3 velocity, Vec3 spread, int count)
    {
        FriendlyByteBuf buf = PacketByteBufs.create();
        particle.writeToNetwork(buf);
        CHANNEL.emitter(player).apply(origin, velocity, spread, count, particle.getType(), buf);
    }

    @Environment(value=Dist.CLIENT)
    public static class Client
    {
        public static void registerReceiver()
        {
            CHANNEL.receiver(Dist.CLIENT, (origin, velocity, spread, count, type, parameters) ->
            {
                if (type != null)
                {
                    ParticleOptions effect = readParameters(type, parameters);
                    Minecraft client = Minecraft.getInstance();

                    for (int i = 0; i < count; ++i)
                    {
                        double px = origin.x + (Math.random() - 0.5) * 2 * spread.x;
                        double py = origin.y + (Math.random() - 0.5) * 2 * spread.y;
                        double pz = origin.z + (Math.random() - 0.5) * 2 * spread.z;
                        client.level.addParticle(effect, px, py, pz, velocity.x, velocity.y, velocity.z);
                    }
                }
            });
        }

        private static <T extends ParticleOptions> T readParameters(ParticleType<T> type, FriendlyByteBuf buf)
        {
            return type.getDeserializer().fromNetwork(type, buf);
        }
    }

    public interface SpawnParticle
    {
        void apply(Vec3 origin, Vec3 velocity, Vec3 spread, int count, ParticleType<?> type, FriendlyByteBuf parameters);
    }
}
