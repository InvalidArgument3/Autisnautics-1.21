/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * The integrator spends {@code data} and replaces the input item.
 */

package com.neep.neepmeat.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record EnlighteningRecipe(ResourceLocation input, int inputCount, ResourceLocation output, int outputCount, int data)
        implements Recipe<ManufactureInput> {
    private static final MapCodec<CountedId> COUNTED = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("resource").forGetter(CountedId::resource),
            Codec.INT.optionalFieldOf("amount", 1).forGetter(CountedId::amount)
    ).apply(instance, CountedId::new));

    public static final MapCodec<EnlighteningRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            COUNTED.fieldOf("item_input").forGetter(recipe -> new CountedId(recipe.input.toString(), recipe.inputCount)),
            COUNTED.fieldOf("output").forGetter(recipe -> new CountedId(recipe.output.toString(), recipe.outputCount)),
            Codec.INT.fieldOf("data").forGetter(EnlighteningRecipe::data)
    ).apply(instance, (input, output, data) -> new EnlighteningRecipe(
            ResourceLocation.parse(input.resource()), input.amount(),
            ResourceLocation.parse(output.resource()), output.amount(), data)));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnlighteningRecipe> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, EnlighteningRecipe::input,
            ByteBufCodecs.INT, EnlighteningRecipe::inputCount,
            ResourceLocation.STREAM_CODEC, EnlighteningRecipe::output,
            ByteBufCodecs.INT, EnlighteningRecipe::outputCount,
            ByteBufCodecs.INT, EnlighteningRecipe::data,
            EnlighteningRecipe::new);

    public static final RecipeSerializer<EnlighteningRecipe> SERIALIZER = new RecipeSerializer<>() {
        @Override
        public MapCodec<EnlighteningRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EnlighteningRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    };

    public boolean matches(ItemStack stack) {
        Item item = BuiltInRegistries.ITEM.getOptional(input).orElse(Items.AIR);
        return stack.is(item) && stack.getCount() >= inputCount;
    }

    public ItemStack resultStack() {
        return new ItemStack(BuiltInRegistries.ITEM.getOptional(output).orElse(Items.AIR), outputCount);
    }

    public ItemStack inputStack() {
        return new ItemStack(BuiltInRegistries.ITEM.getOptional(input).orElse(Items.AIR), inputCount);
    }

    @Override
    public boolean matches(ManufactureInput input, Level level) {
        return matches(input.stack());
    }

    @Override
    public ItemStack assemble(ManufactureInput input, HolderLookup.Provider registries) {
        return resultStack();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return resultStack();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NeepRecipes.ENLIGHTENING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return NeepRecipes.ENLIGHTENING.get();
    }

    private record CountedId(String resource, int amount) {}
}
