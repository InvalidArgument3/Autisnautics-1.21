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

package com.neep.meatlib.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.Optional;

/**
 * Upstream {@code RegistryRecipeInput} / {@code RecipeOutput} shape: a resource or a tag,
 * an amount, and an optional min/max roll.
 */
public record ResourceSpec(Optional<ResourceLocation> resource, Optional<String> tag, long amount, long min, long max, float chance) {
    public static final Codec<ResourceSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("resource").forGetter(ResourceSpec::resource),
            Codec.STRING.optionalFieldOf("tag").forGetter(ResourceSpec::tag),
            Codec.LONG.optionalFieldOf("amount", 1L).forGetter(ResourceSpec::amount),
            Codec.LONG.optionalFieldOf("min", -1L).forGetter(ResourceSpec::min),
            Codec.LONG.optionalFieldOf("max", -1L).forGetter(ResourceSpec::max),
            Codec.FLOAT.optionalFieldOf("chance", 1f).forGetter(ResourceSpec::chance)
    ).apply(instance, ResourceSpec::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResourceSpec> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), ResourceSpec::resource,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), ResourceSpec::tag,
            ByteBufCodecs.VAR_LONG, ResourceSpec::amount,
            ByteBufCodecs.VAR_LONG, ResourceSpec::min,
            ByteBufCodecs.VAR_LONG, ResourceSpec::max,
            ByteBufCodecs.FLOAT, ResourceSpec::chance,
            ResourceSpec::new);

    public boolean acceptsItem(ItemStack stack) {
        if (stack.isEmpty() || stack.getCount() < amount) {
            return false;
        }
        if (resource.isPresent()) {
            return stack.is(BuiltInRegistries.ITEM.getOptional(resource.get()).orElse(Items.AIR));
        }
        if (tag.isPresent()) {
            return stack.is(TagKey.create(Registries.ITEM, ResourceLocation.parse(tag.get())));
        }
        return false;
    }

    public boolean acceptsFluid(Fluid fluid, long stored) {
        if (stored < amount || fluid == null || fluid == Fluids.EMPTY) {
            return false;
        }
        if (resource.isPresent()) {
            return BuiltInRegistries.FLUID.getOptional(resource.get()).orElse(Fluids.EMPTY) == fluid;
        }
        if (tag.isPresent()) {
            return fluid.defaultFluidState().is(TagKey.create(Registries.FLUID, ResourceLocation.parse(tag.get())));
        }
        return false;
    }

    public ItemStack rollItem(RandomSource random) {
        Item item = resource.map(id -> BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR)).orElse(Items.AIR);
        return new ItemStack(item, (int) roll(random));
    }

    public Fluid fluid() {
        return resource.map(id -> BuiltInRegistries.FLUID.getOptional(id).orElse(Fluids.EMPTY)).orElse(Fluids.EMPTY);
    }

    public long roll(RandomSource random) {
        if (min >= 0 && max >= min) {
            if (min == max) {
                return min;
            }
            return min + random.nextInt((int) (max - min + 1));
        }
        return amount;
    }
}
