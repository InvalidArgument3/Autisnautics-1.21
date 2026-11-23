/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record CountedIngredient(Holder<Item> item, int amount) {
    public static final Codec<CountedIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("resource").forGetter(CountedIngredient::item),
            Codec.INT.fieldOf("amount").forGetter(CountedIngredient::amount)
    ).apply(instance, CountedIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CountedIngredient> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ITEM), CountedIngredient::item,
            ByteBufCodecs.INT, CountedIngredient::amount,
            CountedIngredient::new);

    public ItemStack stack() {
        return new ItemStack(item, amount);
    }

    public boolean test(ItemStack stack) {
        return stack.is(item) && stack.getCount() >= amount;
    }
}
