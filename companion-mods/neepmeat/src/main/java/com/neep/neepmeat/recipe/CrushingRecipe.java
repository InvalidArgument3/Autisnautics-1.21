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

package com.neep.neepmeat.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neep.meatlib.recipe.ResourceSpec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.Optional;

public final class CrushingRecipe implements Recipe<SingleRecipeInput>, com.neep.meatlib.recipe.RecipeContextMatch {
    private final boolean destroy;
    private final ResourceSpec input;
    private final ResourceSpec output;
    private final ResourceSpec extra;
    private final float experience;
    private final int time;
    private final RecipeType<?> type;
    private final RecipeSerializer<?> serializer;

    public CrushingRecipe(boolean destroy, ResourceSpec input, ResourceSpec output, ResourceSpec extra, float experience, int time, RecipeType<?> type, RecipeSerializer<?> serializer) {
        this.destroy = destroy;
        this.input = input;
        this.output = output;
        this.extra = extra;
        this.experience = experience;
        this.time = time;
        this.type = type;
        this.serializer = serializer;
    }

    public static MapCodec<CrushingRecipe> codec(RecipeType<?> type, RecipeSerializer<?> serializer, int defaultTime) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("destroy", false).forGetter(CrushingRecipe::destroy),
                ResourceSpec.CODEC.optionalFieldOf("input").forGetter(recipe -> Optional.ofNullable(recipe.input)),
                ResourceSpec.CODEC.optionalFieldOf("output").forGetter(recipe -> Optional.ofNullable(recipe.output)),
                ResourceSpec.CODEC.optionalFieldOf("extra").forGetter(recipe -> Optional.ofNullable(recipe.extra)),
                Codec.FLOAT.optionalFieldOf("experience", 0f).forGetter(CrushingRecipe::experience),
                Codec.INT.optionalFieldOf("processtime", defaultTime).forGetter(CrushingRecipe::time)
        ).apply(instance, (destroy, input, output, extra, experience, time) -> new CrushingRecipe(
                destroy, input.orElse(null), output.orElse(null), extra.orElse(null), experience, time, type, serializer)));
    }

    public static StreamCodec<RegistryFriendlyByteBuf, CrushingRecipe> streamCodec(RecipeType<?> type, RecipeSerializer<?> serializer) {
        return StreamCodec.composite(
                ByteBufCodecs.BOOL, CrushingRecipe::destroy,
                ByteBufCodecs.optional(ResourceSpec.STREAM_CODEC), recipe -> Optional.ofNullable(recipe.input),
                ByteBufCodecs.optional(ResourceSpec.STREAM_CODEC), recipe -> Optional.ofNullable(recipe.output),
                ByteBufCodecs.optional(ResourceSpec.STREAM_CODEC), recipe -> Optional.ofNullable(recipe.extra),
                ByteBufCodecs.FLOAT, CrushingRecipe::experience,
                ByteBufCodecs.INT, CrushingRecipe::time,
                (destroy, input, output, extra, experience, time) -> new CrushingRecipe(
                        destroy, input.orElse(null), output.orElse(null), extra.orElse(null), experience, time, type, serializer));
    }

    public boolean destroy() {
        return destroy;
    }

    public float experience() {
        return experience;
    }

    public int time() {
        return time;
    }

    public long inputAmount() {
        return input == null ? 0 : input.amount();
    }

    public boolean accepts(ItemStack stack) {
        return !destroy && input != null && input.acceptsItem(stack);
    }

    @Override
    public boolean matches(Object input, Level level) {
        if (input instanceof ItemStack stack) {
            return accepts(stack);
        }
        if (input instanceof SingleRecipeInput recipeInput) {
            return accepts(recipeInput.item());
        }
        return false;
    }

    public ItemStack rollOutput(RandomSource random) {
        if (output == null) {
            return ItemStack.EMPTY;
        }
        return output.rollItem(random);
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return accepts(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return rollOutput(RandomSource.create());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output == null ? ItemStack.EMPTY : output.rollItem(RandomSource.create(1));
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return serializer;
    }

    @Override
    public RecipeType<?> getType() {
        return type;
    }

    /**
     * The 1.20 serializer took separate normal and destroy factories. The destroy flag
     * is now a field of this recipe, so one serializer reads both.
     */
    public static final class Serializer implements RecipeSerializer<CrushingRecipe> {
        private final int time;
        private MapCodec<CrushingRecipe> map;
        private StreamCodec<RegistryFriendlyByteBuf, CrushingRecipe> stream;

        public Serializer(int time) {
            this.time = time;
        }

        @Override
        public MapCodec<CrushingRecipe> codec() {
            if (map == null) {
                map = CrushingRecipe.codec(null, this, time);
            }
            return map;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CrushingRecipe> streamCodec() {
            if (stream == null) {
                stream = CrushingRecipe.streamCodec(null, this);
            }
            return stream;
        }
    }
}
