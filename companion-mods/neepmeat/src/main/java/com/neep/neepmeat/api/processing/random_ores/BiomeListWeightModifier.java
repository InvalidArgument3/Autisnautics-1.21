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

package com.neep.neepmeat.api.processing.random_ores;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public class BiomeListWeightModifier implements WeightModifier
{
    public static final Codec<BiomeListWeightModifier> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                    Codec.list(ResourceLocation.CODEC).fieldOf("biomes").forGetter(m -> m.biomes),
                    Codec.FLOAT.fieldOf("value").forGetter(m -> m.value),
                    Function.CODEC.fieldOf("function").forGetter(m -> m.function)
                ).apply(instance, BiomeListWeightModifier::new));

    private final List<ResourceLocation> biomes;
    private final Set<ResourceLocation> biomeSet;
    private final float value;
    private final Function function;

    public BiomeListWeightModifier(List<ResourceLocation> biomes, float value, Function function)
    {
        this.biomes = biomes;
        this.value = value;
        this.function = function;
        this.biomeSet = new HashSet<>(biomes);
    }

    @Override
    public float apply(float base, Level world, BlockPos pos)
    {
        Holder<Biome> biome = world.getBiome(pos);
        ResourceKey<Biome> key = biome.unwrapKey().orElse(null);

        if (key != null && biomeSet.contains(key.location()))
        {
            return switch (function)
            {
                case ADD -> base + value;
                case MUL -> base * value;
            };
        }

        return base;
    }

    @Override
    public Codec<? extends WeightModifier> getCodec()
    {
        return CODEC;
    }
}
