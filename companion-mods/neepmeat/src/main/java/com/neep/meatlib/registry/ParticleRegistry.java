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

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Function;

public class ParticleRegistry
{
    public static <T extends ParticleType<?>> T register(String namespace, String id, T particleType)
    {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(namespace, id), particleType);
    }

    public static <T extends ParticleOptions> ParticleType<T> register(String namespace, String id, ParticleOptions.Deserializer<T> factory, final Function<ParticleType<T>, Codec<T>> function)
    {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(namespace, id), new ParticleType<T>(false, factory)
        {
            public Codec<T> codec()
            {
                return function.apply(this);
            }
        });
    }

    public static <T extends ParticleOptions> ParticleProvider<T> emptyFactory(FabricSpriteProvider provider)
    {
        return (parameters, world1, x, y, z, velocityX, velocityY, velocityZ) -> null;
    }
}
