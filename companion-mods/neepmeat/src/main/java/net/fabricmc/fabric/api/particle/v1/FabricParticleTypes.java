/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package net.fabricmc.fabric.api.particle.v1;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class FabricParticleTypes {
    private FabricParticleTypes() {}

    public static SimpleParticleType simple() {
        return new SimpleParticleType(false);
    }

    public static <T extends ParticleOptions> ParticleType<T> complex(Object ignoredDeserializer) {
        return new ParticleType<>(false) {
            @Override
            public MapCodec<T> codec() {
                return null;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                return null;
            }
        };
    }
}
