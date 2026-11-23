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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public class BiomeTagWeightModifier implements WeightModifier
{
    public static final Codec<BiomeTagWeightModifier> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                    TagKey.hashedCodec(Registries.BIOME).fieldOf("tag").forGetter(m -> m.tag),
                    Codec.FLOAT.fieldOf("value").forGetter(m -> m.value),
                    Function.CODEC.fieldOf("function").forGetter(m -> m.function)
                ).apply(instance, BiomeTagWeightModifier::new));

    private final TagKey<Biome> tag;
    private final float value;
    private final Function function;

    public BiomeTagWeightModifier(TagKey<Biome> tag, float value, Function function)
    {
        this.tag = tag;
        this.value = value;
        this.function = function;
    }

    @Override
    public float apply(float base, Level world, BlockPos pos)
    {
        Holder<Biome> biome = world.getBiome(pos);

        if (biome.is(tag))
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
