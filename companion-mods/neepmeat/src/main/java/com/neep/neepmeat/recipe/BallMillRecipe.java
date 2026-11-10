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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neep.meatlib.recipe.ResourceSpec;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
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

import java.util.List;

public final class BallMillRecipe implements Recipe<SingleRecipeInput>, com.neep.meatlib.recipe.RecipeContextMatch {
    private final ResourceSpec input;
    private final List<ResourceSpec> output;
    private final RecipeType<?> type;
    private final RecipeSerializer<?> serializer;

    public BallMillRecipe(ResourceSpec input, List<ResourceSpec> output, RecipeType<?> type, RecipeSerializer<?> serializer) {
        this.input = input;
        this.output = output;
        this.type = type;
        this.serializer = serializer;
    }

    public static MapCodec<BallMillRecipe> codec(RecipeType<?> type, RecipeSerializer<?> serializer) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceSpec.CODEC.fieldOf("input").forGetter(recipe -> recipe.input),
                ResourceSpec.CODEC.listOf().fieldOf("output").forGetter(recipe -> recipe.output)
        ).apply(instance, (input, output) -> new BallMillRecipe(input, output, type, serializer)));
    }

    public static StreamCodec<RegistryFriendlyByteBuf, BallMillRecipe> streamCodec(RecipeType<?> type, RecipeSerializer<?> serializer) {
        return StreamCodec.composite(
                ResourceSpec.STREAM_CODEC, recipe -> recipe.input,
                ResourceSpec.STREAM_CODEC.apply(ByteBufCodecs.list()), recipe -> recipe.output,
                (input, output) -> new BallMillRecipe(input, output, type, serializer));
    }

    public boolean accepts(ItemStack stack) {
        return input.acceptsItem(stack);
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

    public long inputAmount() {
        return input.amount();
    }

    public boolean eject(Storage<ItemVariant> outputStorage, TransactionContext transaction) {
        try (Transaction inner = ((Transaction) transaction).openNested()) {
            RandomSource random = RandomSource.create();
            for (ResourceSpec spec : output) {
                if (spec.chance() < 1f && random.nextFloat() > spec.chance()) {
                    continue;
                }
                ItemStack stack = spec.rollItem(random);
                long moved = outputStorage.insert(ItemVariant.of(stack), stack.getCount(), inner);
                if (moved != stack.getCount()) {
                    return false;
                }
            }
            inner.commit();
            return true;
        }
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return accepts(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return output.isEmpty() ? ItemStack.EMPTY : output.getFirst().rollItem(RandomSource.create(1));
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return assemble(null, registries);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return serializer;
    }

    @Override
    public RecipeType<?> getType() {
        return type;
    }
}
