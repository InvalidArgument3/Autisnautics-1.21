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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.ParamCodecs;
import com.neep.meatlib.api.network.RecordParamCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class PressureWashParticleType extends ParticleType<PressureWashParticleType.Parameters>
{
    private final Codec<Parameters> codec;
    private final ParamCodec<Parameters> paramCodec;

    public static Codec<Parameters> createCodec(PressureWashParticleType type)
    {
        return RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.FLOAT.fieldOf("gravity").forGetter(Parameters::gravity),
                Codec.INT.fieldOf("col").forGetter(Parameters::col),
                BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(Parameters::fluid)
            ).apply(instance, (gravity, col, fluid) -> new Parameters(type, gravity, col, fluid))
        );
    }

    public static ParamCodec<Parameters> createParamCodec(PressureWashParticleType type)
    {
        return RecordParamCodec.builder(Parameters.class)
                .param(ParamCodecs.constant(PressureWashParticleType.class, type))
                .param(ParamCodec.FLOAT)
                .param(ParamCodec.INT)
                .param(ParamCodecs.registryValue(Fluid.class, BuiltInRegistries.FLUID))
                .build();
    }

    public static final ParticleOptions.Deserializer<Parameters> PARAMETER_FACTORY = new ParticleOptions.Deserializer<>()
    {
        @Override
        public Parameters fromCommand(ParticleType<Parameters> type, StringReader reader) throws CommandSyntaxException
        {
            throw new CommandSyntaxException(new SimpleCommandExceptionType(Component.nullToEmpty("not supported")), Component.nullToEmpty("not supported"));
        }

        @Override
        public Parameters fromNetwork(ParticleType<Parameters> type, FriendlyByteBuf buf)
        {
            return ((PressureWashParticleType) type).paramCodec.decode(buf);
        }
    };

    public PressureWashParticleType(boolean alwaysShow)
    {
        super(alwaysShow, PARAMETER_FACTORY);
        this.codec = createCodec(this);
        paramCodec = createParamCodec(this);
    }

    @Override
    public Codec<Parameters> codec()
    {
        return codec;
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

        @Override
        public void writeToNetwork(FriendlyByteBuf buf)
        {
            type.paramCodec.encode(this, buf);
        }

        @Override
        public String writeToString()
        {
            return "???";
        }
    }
}
