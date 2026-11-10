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
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SwirlingParticleEffect implements ParticleOptions
{
    /** Command form kept for the old particle argument. 1.21 sends the effect through {@link #streamCodec}. */
    public static SwirlingParticleEffect fromCommand(ParticleType<SwirlingParticleEffect> particleType, StringReader stringReader) throws CommandSyntaxException
    {
        stringReader.expect(' ');
        BlockState state = (BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), stringReader, false)).blockState();
        stringReader.expect(' ');
        double radius = stringReader.readDouble();
        stringReader.expect(' ');
        double speed = stringReader.readDouble();
        return new SwirlingParticleEffect(particleType, state, radius, speed);
    }

    private final ParticleType<SwirlingParticleEffect> type;
    private final BlockState blockState;
    public final double radius;
    public final double speed;

    public SwirlingParticleEffect(ParticleType<SwirlingParticleEffect> type, BlockState state, double radius, double angularSpeed)
    {
        this.type = type;
        this.blockState = state;
        this.radius = radius;
        this.speed = angularSpeed;
    }

    public static MapCodec<SwirlingParticleEffect> createMapCodec(ParticleType<SwirlingParticleEffect> type)
    {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                        (BlockState.CODEC.fieldOf("blockstate")).forGetter(effect -> effect.blockState),
                        (Codec.DOUBLE.fieldOf("radius")).forGetter(effect -> effect.radius),
                        (Codec.DOUBLE.fieldOf("speed")).forGetter(effect -> effect.speed)
                        ).apply(instance, (state, radius, speed) -> new SwirlingParticleEffect(type, state, radius, speed)));
    }

    public static Codec<SwirlingParticleEffect> createCodec(ParticleType<SwirlingParticleEffect> type)
    {
        return createMapCodec(type).codec();
    }

    /** Block state, radius, and angular speed. The 1.20 writer dropped the two doubles that the reader consumed. */
    public static StreamCodec<RegistryFriendlyByteBuf, SwirlingParticleEffect> streamCodec(ParticleType<SwirlingParticleEffect> type)
    {
        return StreamCodec.of(
                (buf, effect) -> {
                    buf.writeVarInt(Block.BLOCK_STATE_REGISTRY.getId(effect.blockState));
                    buf.writeDouble(effect.radius);
                    buf.writeDouble(effect.speed);
                },
                buf -> new SwirlingParticleEffect(type, Block.BLOCK_STATE_REGISTRY.byId(buf.readVarInt()), buf.readDouble(), buf.readDouble()));
    }

    @Override
    public ParticleType<?> getType()
    {
        return type;
    }

    public void writeToNetwork(FriendlyByteBuf buf)
    {
        buf.writeVarInt(Block.BLOCK_STATE_REGISTRY.getId(this.blockState));
        buf.writeDouble(this.radius);
        buf.writeDouble(this.speed);
    }

    public String writeToString()
    {
        return BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()) + " " + BlockStateParser.serialize(this.blockState);
    }

    public BlockState getBlockState()
    {
        return blockState;
    }
}
