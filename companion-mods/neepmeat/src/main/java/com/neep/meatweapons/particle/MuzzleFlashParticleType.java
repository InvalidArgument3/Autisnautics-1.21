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

package com.neep.meatweapons.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import java.util.UUID;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

public class MuzzleFlashParticleType extends ParticleType<MuzzleFlashParticleType.MuzzleFlashParticleEffect>
{
    public static final Codec<UUID> UUID_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.fieldOf("msb").forGetter(UUID::getMostSignificantBits),
                    Codec.LONG.fieldOf("lsb").forGetter(UUID::getLeastSignificantBits)
            ).apply(instance, UUID::new));

    public static MapCodec<MuzzleFlashParticleEffect> createMapCodec(ParticleType<MuzzleFlashParticleEffect> type)
    {
        return RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                UUID_CODEC.fieldOf("uuid").forGetter(e -> e.playerUUID),
                                Codec.DOUBLE.fieldOf("dx").forGetter(e -> e.dx),
                                Codec.DOUBLE.fieldOf("dy").forGetter(e -> e.dy),
                                Codec.DOUBLE.fieldOf("dz").forGetter(e -> e.dz),
                                Codec.FLOAT.fieldOf("scale").forGetter(e -> e.scale),
                                Codec.INT.fieldOf("max_age").forGetter(e -> e.maxAge),
                                Codec.STRING.xmap(s -> InteractionHand.valueOf(InteractionHand.class, s), InteractionHand::name).fieldOf("hand").forGetter(e -> e.hand))
                        .apply(instance, (uuid, aDouble, aDouble2, aDouble3, aFloat, maxAge, hand) ->
                                new MuzzleFlashParticleEffect(type, uuid, aDouble, aDouble2, aDouble3, aFloat, maxAge, hand))
        );
    }

    public static Codec<MuzzleFlashParticleEffect> createCodec(ParticleType<MuzzleFlashParticleEffect> type)
    {
        return createMapCodec(type).codec();
    }

    private final MapCodec<MuzzleFlashParticleEffect> codec;

    protected MuzzleFlashParticleType(boolean alwaysShow, Codec<MuzzleFlashParticleEffect> codec)
    {
        super(alwaysShow);
        this.codec = MapCodec.assumeMapUnsafe(codec);
    }

    @Override
    public MapCodec<MuzzleFlashParticleEffect> codec()
    {
        return codec;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, MuzzleFlashParticleEffect> streamCodec()
    {
        return StreamCodec.of(
                (buf, effect) -> {
                    buf.writeUUID(effect.playerUUID);
                    buf.writeDouble(effect.dx);
                    buf.writeDouble(effect.dy);
                    buf.writeDouble(effect.dz);
                    buf.writeFloat(effect.scale);
                    buf.writeInt(effect.maxAge);
                    buf.writeEnum(effect.hand);
                },
                buf -> new MuzzleFlashParticleEffect(this, buf.readUUID(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readInt(), buf.readEnum(InteractionHand.class)));
    }

    public static class MuzzleFlashParticleEffect implements ParticleOptions
    {
        private final ParticleType<MuzzleFlashParticleEffect> type;
        private final UUID playerUUID;
        public final double dx, dy, dz;
        public final float scale;
        public final int maxAge;
        public final InteractionHand hand;

        public MuzzleFlashParticleEffect(ParticleType<MuzzleFlashParticleEffect> type, LivingEntity player, double dx, double dy, double dz, float scale, int maxAge, InteractionHand hand)
        {
            this(type, player.getUUID(), dx, dy, dz, scale, maxAge, hand);
        }

        public MuzzleFlashParticleEffect(ParticleType<MuzzleFlashParticleEffect> type, UUID uuid, double dx, double dy, double dz, float scale, int maxAge, InteractionHand hand)
        {
            this.type = type;
            this.playerUUID = uuid;
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
            this.scale = scale;
            this.maxAge = maxAge;
            this.hand = hand;
        }

        @Override
        public ParticleType<MuzzleFlashParticleEffect> getType()
        {
            return type;
        }

        public void writeToNetwork(FriendlyByteBuf buf)
        {
            buf.writeById(BuiltInRegistries.PARTICLE_TYPE::getId, getType());
            buf.writeUUID(playerUUID);
            buf.writeDouble(dx);
            buf.writeDouble(dy);
            buf.writeDouble(dz);
            buf.writeFloat(scale);
            buf.writeInt(maxAge);
            buf.writeEnum(hand);
        }

        public UUID getPlayerUUID()
        {
            return playerUUID;
        }

        public String writeToString()
        {
            return BuiltInRegistries.PARTICLE_TYPE.getKey(getType()).toString();
        }
    }
}
