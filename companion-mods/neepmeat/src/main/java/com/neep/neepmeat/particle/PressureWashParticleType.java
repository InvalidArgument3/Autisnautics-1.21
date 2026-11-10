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

package com.neep.neepmeat.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class PressureWashParticleType extends ParticleType<PressureWashParticleType.Parameters>
{
    private final MapCodec<Parameters> codec;
    private StreamCodec<? super RegistryFriendlyByteBuf, Parameters> network;

    public static MapCodec<Parameters> createMapCodec(PressureWashParticleType type)
    {
        return RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                Codec.FLOAT.fieldOf("gravity").forGetter(Parameters::gravity),
                Codec.INT.fieldOf("col").forGetter(Parameters::col),
                BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(Parameters::fluid)
            ).apply(instance, (gravity, col, fluid) -> new Parameters(type, gravity, col, fluid))
        );
    }

    public static Codec<Parameters> createCodec(PressureWashParticleType type)
    {
        return createMapCodec(type).codec();
    }

    public PressureWashParticleType(boolean alwaysShow)
    {
        super(alwaysShow);
        this.codec = createMapCodec(this);
    }

    @Override
    public MapCodec<Parameters> codec()
    {
        return codec;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, Parameters> streamCodec()
    {
        if (network == null) {
            network = StreamCodec.of(
                (buf, value) -> {
                    buf.writeFloat(value.gravity());
                    buf.writeInt(value.col());
                    buf.writeById(BuiltInRegistries.FLUID::getId, value.fluid());
                },
                buf -> new Parameters(this, buf.readFloat(), buf.readInt(), buf.readById(BuiltInRegistries.FLUID::byId)));
        }
        return network;
    }

    public record Parameters(PressureWashParticleType type, float gravity, int col, Fluid fluid) implements ParticleOptions
    {
        public Parameters(PressureWashParticleType type, float gravity)
        {
            this(type, gravity, 0xffffff, Fluids.EMPTY);
        }

        @Override
        public ParticleType<?> getType()
        {
            return type;
        }

        public void writeToNetwork(FriendlyByteBuf buf)
        {
            buf.writeFloat(gravity);
            buf.writeInt(col);
            buf.writeById(BuiltInRegistries.FLUID::getId, fluid);
        }

        public String writeToString()
        {
            return "???";
        }
    }
}
