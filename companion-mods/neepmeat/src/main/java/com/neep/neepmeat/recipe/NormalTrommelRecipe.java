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
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import com.neep.neepmeat.machine.small_trommel.TrommelRecipe;
import com.neep.neepmeat.machine.small_trommel.TrommelStorage;
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

public final class NormalTrommelRecipe implements Recipe<SingleRecipeInput>, TrommelRecipe {
    private final ResourceSpec input;
    private final ResourceSpec output;
    private final ResourceSpec aux;
    private final RecipeType<?> type;
    private final RecipeSerializer<?> serializer;

    public NormalTrommelRecipe(ResourceSpec input, ResourceSpec output, ResourceSpec aux, RecipeType<?> type, RecipeSerializer<?> serializer) {
        this.input = input;
        this.output = output;
        this.aux = aux;
        this.type = type;
        this.serializer = serializer;
    }

    public static MapCodec<NormalTrommelRecipe> codec(RecipeType<?> type, RecipeSerializer<?> serializer) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceSpec.CODEC.fieldOf("input").forGetter(recipe -> recipe.input),
                ResourceSpec.CODEC.fieldOf("output").forGetter(recipe -> recipe.output),
                ResourceSpec.CODEC.optionalFieldOf("aux_output").forGetter(recipe -> Optional.ofNullable(recipe.aux))
        ).apply(instance, (input, output, aux) -> new NormalTrommelRecipe(input, output, aux.orElse(null), type, serializer)));
    }

    public static StreamCodec<RegistryFriendlyByteBuf, NormalTrommelRecipe> streamCodec(RecipeType<?> type, RecipeSerializer<?> serializer) {
        return StreamCodec.composite(
                ResourceSpec.STREAM_CODEC, recipe -> recipe.input,
                ResourceSpec.STREAM_CODEC, recipe -> recipe.output,
                ByteBufCodecs.optional(ResourceSpec.STREAM_CODEC), recipe -> Optional.ofNullable(recipe.aux),
                (input, output, aux) -> new NormalTrommelRecipe(input, output, aux.orElse(null), type, serializer));
    }

    @Override
    public boolean matches(TrommelStorage inventory) {
        return input.acceptsFluid(inventory.input().getResource().fluid(), inventory.input().getAmount());
    }

    @Override
    public boolean takeInputs(TrommelStorage storage, TransactionContext transaction) {
        try (Transaction inner = ((Transaction) transaction).openNested()) {
            long extracted = storage.input().extract(storage.input().getResource(), input.amount(), inner);
            if (extracted == input.amount()) {
                inner.commit();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean ejectOutputs(TrommelStorage storage, TransactionContext transaction) {
        try (Transaction inner = ((Transaction) transaction).openNested()) {
            long amount = output.roll(RandomSource.create());
            long inserted = storage.output().insert(FluidVariant.of(output.fluid()), amount, inner);
            boolean itemOk = true;
            if (aux != null && aux.chance() >= 1f) {
                ItemStack stack = aux.rollItem(RandomSource.create());
                long moved = storage.itemOutput().insert(ItemVariant.of(stack), stack.getCount(), inner);
                itemOk = moved == stack.getCount();
            }
            if (inserted == amount && itemOk) {
                inner.commit();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return aux == null ? ItemStack.EMPTY : aux.rollItem(RandomSource.create(1));
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return serializer;
    }

    @Override
    public RecipeType<?> getType() {
        return type;
    }

    public static final class NetworkSerializer implements RecipeSerializer<NormalTrommelRecipe> {
        private MapCodec<NormalTrommelRecipe> map;
        private StreamCodec<RegistryFriendlyByteBuf, NormalTrommelRecipe> stream;

        @Override
        public MapCodec<NormalTrommelRecipe> codec() {
            if (map == null) {
                map = NormalTrommelRecipe.codec(null, this);
            }
            return map;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, NormalTrommelRecipe> streamCodec() {
            if (stream == null) {
                stream = NormalTrommelRecipe.streamCodec(null, this);
            }
            return stream;
        }
    }
}
