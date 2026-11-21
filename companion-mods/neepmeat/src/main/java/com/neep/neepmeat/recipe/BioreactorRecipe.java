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
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.Transaction;
import com.neep.neepmeat.machine.live_machine.process.BioreactorProcess;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

public record BioreactorRecipe(List<Spec> inputs, Spec output, int processTime) implements Recipe<SingleRecipeInput> {
    public record Spec(String type, ResourceLocation resource, long amount) {
        public static final Codec<Spec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("type", "minecraft:fluid").forGetter(Spec::type),
                ResourceLocation.CODEC.fieldOf("resource").forGetter(Spec::resource),
                Codec.LONG.fieldOf("amount").forGetter(Spec::amount)
        ).apply(instance, Spec::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Spec> STREAM = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Spec::type,
                ResourceLocation.STREAM_CODEC, Spec::resource,
                ByteBufCodecs.VAR_LONG, Spec::amount,
                Spec::new);

        public Fluid fluid() {
            return BuiltInRegistries.FLUID.getOptional(resource).orElse(Fluids.EMPTY);
        }
    }

    public static final MapCodec<BioreactorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Spec.CODEC.listOf().fieldOf("inputs").forGetter(BioreactorRecipe::inputs),
            Spec.CODEC.fieldOf("output").forGetter(BioreactorRecipe::output),
            Codec.INT.fieldOf("process_time").forGetter(BioreactorRecipe::processTime)
    ).apply(instance, BioreactorRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BioreactorRecipe> STREAM = StreamCodec.composite(
            Spec.STREAM.apply(ByteBufCodecs.list()), BioreactorRecipe::inputs,
            Spec.STREAM, BioreactorRecipe::output,
            ByteBufCodecs.INT, BioreactorRecipe::processTime,
            BioreactorRecipe::new);

    public static final RecipeSerializer<BioreactorRecipe> SERIALIZER = new RecipeSerializer<>() {
        @Override
        public MapCodec<BioreactorRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BioreactorRecipe> streamCodec() {
            return STREAM;
        }
    };

    public boolean accepts(BioreactorProcess.Context context) {
        for (Spec input : inputs) {
            if (!"minecraft:fluid".equals(input.type())) {
                continue;
            }
            if (!has(context.fluidInput(), input)) {
                return false;
            }
        }
        return true;
    }

    public boolean takeInputs(BioreactorProcess.Context context, Transaction transaction) {
        try (Transaction inner = transaction.openNested()) {
            for (Spec input : inputs) {
                if (!"minecraft:fluid".equals(input.type())) {
                    continue;
                }
                if (context.fluidInput().extract(FluidVariant.of(input.fluid()), input.amount(), inner) != input.amount()) {
                    return false;
                }
            }
            inner.commit();
            return true;
        }
    }

    public boolean ejectOutputs(BioreactorProcess.Context context, Transaction transaction) {
        try (Transaction inner = transaction.openNested()) {
            if (context.fluidOutput().insert(FluidVariant.of(output.fluid()), output.amount(), inner) == output.amount()) {
                inner.commit();
                return true;
            }
            return false;
        }
    }

    public int getProcessTime() {
        return processTime;
    }

    private static boolean has(Storage<FluidVariant> storage, Spec spec) {
        try (Transaction transaction = Transaction.openOuter()) {
            boolean enough = storage.extract(FluidVariant.of(spec.fluid()), spec.amount(), transaction) == spec.amount();
            transaction.abort();
            return enough;
        }
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
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return NeepRecipes.BIOREACTOR.get();
    }
}
