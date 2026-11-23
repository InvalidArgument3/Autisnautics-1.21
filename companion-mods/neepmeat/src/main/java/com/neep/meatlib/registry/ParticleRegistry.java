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

package com.neep.meatlib.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class ParticleRegistry
{
    public static <T extends ParticleType<?>> T register(String namespace, String id, T particleType)
    {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(namespace, id), particleType);
    }

    /**
     * 1.21 particle types carry a map codec and a stream codec. The stream codec is what the
     * client receives, so it has to write every field the options hold.
     */
    public static <T extends ParticleOptions> ParticleType<T> register(
            String namespace,
            String id,
            Function<ParticleType<T>, MapCodec<T>> codec,
            Function<ParticleType<T>, StreamCodec<? super RegistryFriendlyByteBuf, T>> stream)
    {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(namespace, id), new ParticleType<T>(false)
        {
            private MapCodec<T> map;
            private StreamCodec<? super RegistryFriendlyByteBuf, T> network;

            @Override
            public MapCodec<T> codec()
            {
                if (map == null) {
                    map = codec.apply(this);
                }
                return map;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec()
            {
                if (network == null) {
                    network = stream.apply(this);
                }
                return network;
            }
        });
    }
}
