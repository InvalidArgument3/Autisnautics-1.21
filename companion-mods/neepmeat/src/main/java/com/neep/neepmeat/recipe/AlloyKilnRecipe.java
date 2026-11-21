/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record AlloyKilnRecipe(CountedIngredient input1, CountedIngredient input2, CountedIngredient output, int processTime) implements Recipe<AlloyKilnInput> {
    public static final MapCodec<AlloyKilnRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CountedIngredient.CODEC.fieldOf("input1").forGetter(AlloyKilnRecipe::input1),
            CountedIngredient.CODEC.fieldOf("input2").forGetter(AlloyKilnRecipe::input2),
            CountedIngredient.CODEC.fieldOf("output").forGetter(AlloyKilnRecipe::output),
            Codec.INT.optionalFieldOf("processtime", 200).forGetter(AlloyKilnRecipe::processTime)
    ).apply(instance, AlloyKilnRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AlloyKilnRecipe> STREAM_CODEC = StreamCodec.composite(
            CountedIngredient.STREAM_CODEC, AlloyKilnRecipe::input1,
            CountedIngredient.STREAM_CODEC, AlloyKilnRecipe::input2,
            CountedIngredient.STREAM_CODEC, AlloyKilnRecipe::output,
            ByteBufCodecs.INT, AlloyKilnRecipe::processTime,
            AlloyKilnRecipe::new);

    public static final RecipeSerializer<AlloyKilnRecipe> SERIALIZER = new RecipeSerializer<>() {
        @Override
        public MapCodec<AlloyKilnRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AlloyKilnRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    };

    @Override
    public boolean matches(AlloyKilnInput input, Level level) {
        return fits(input.first(), input.second()) || fits(input.second(), input.first());
    }

    private boolean fits(ItemStack first, ItemStack second) {
        return input1.test(first) && input2.test(second);
    }

    @Override
    public ItemStack assemble(AlloyKilnInput input, HolderLookup.Provider registries) {
        return output.stack();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.stack();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NeepRecipes.ALLOY_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return NeepRecipes.ALLOY.get();
    }
}
