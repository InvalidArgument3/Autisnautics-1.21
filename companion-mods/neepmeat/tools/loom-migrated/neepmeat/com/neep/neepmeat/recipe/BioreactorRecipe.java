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

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neep.meatlib.network.PacketBufUtil;
import com.neep.meatlib.recipe.MeatRecipeSerialiser;
import com.neep.meatlib.recipe.MeatlibRecipe;
import com.neep.meatlib.util.MeatLibCodecs;
import com.neep.meatlib.recipe.ingredient.*;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.live_machine.process.BioreactorProcess;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import java.util.ArrayList;
import java.util.List;

public class BioreactorRecipe implements MeatlibRecipe<BioreactorProcess.Context>
{
    private final ResourceLocation id;
    private final List<RegistryRecipeInput<Item>> itemInputs;
    private final List<RegistryRecipeInput<Fluid>> fluidInputs;
    private final RecipeOutput<Fluid> output;
    private final int processTime;

    public BioreactorRecipe(ResourceLocation id, List<RegistryRecipeInput<Item>> itemInputs, List<RegistryRecipeInput<Fluid>> fluidInputs, RecipeOutput<Fluid> output, int processTime)
    {
        this.id = id;
        this.itemInputs = itemInputs;
        this.fluidInputs = fluidInputs;
        this.output = output;
        this.processTime = processTime;
    }

    @Override
    public boolean matches(BioreactorProcess.Context context)
    {
        int satisfied = itemInputs.size();
        for (var input : itemInputs)
        {
            if (input.testStorage(context.itemInput()))
                satisfied--;
        }
        for (var input : fluidInputs)
        {
            if (input.testStorage(context.fluidInput()))
                satisfied--;
        }

        return satisfied <= 0;
    }

    @Override
    public boolean takeInputs(BioreactorProcess.Context context, TransactionContext transaction)
    {
        try (Transaction inner = transaction.openNested())
        {
            for (var input : itemInputs)
            {
                if (!input.extract(context.itemInput(), ItemVariant::of, inner))
                    return false;
            }

            for (var input : fluidInputs)
            {
                if (!input.extract(context.fluidInput(), FluidVariant::of, inner))
                    return false;
            }

            inner.commit();
            return true;
        }
    }

    @Override
    public boolean ejectOutputs(BioreactorProcess.Context context, TransactionContext transaction)
    {
        try (Transaction inner = transaction.openNested())
        {
            if (output.insertInto(context.fluidOutput(), FluidVariant::of, inner))
            {
                inner.commit();
                return true;
            }
        }
        return false;
    }

    @Override
    public MeatRecipeSerialiser<?> getSerializer()
    {
        return NMrecipeTypes.BIOREACTOR_SERIALISER;
    }

    @Override
    public RecipeType<?> getType()
    {
        return NMrecipeTypes.BIOREACTOR;
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    public int getProcessTime()
    {
        return processTime;
    }

    public List<RegistryRecipeInput<Item>> getItemInputs()
    {
        return itemInputs;
    }

    public List<RegistryRecipeInput<Fluid>> getFluidInputs()
    {
        return fluidInputs;
    }

    public RecipeOutput<Fluid> getOutputs()
    {
        return output;
    }

    public static class Serialiser implements MeatRecipeSerialiser<BioreactorRecipe>
    {
        private final Codec<RecipeFunction<BioreactorRecipe>> codec = RecordCodecBuilder.create(instance -> instance.group(
                Codec.list(RecipeInputs.ANY).flatComapMap(Serialiser::sortInputs, r -> DataResult.error(() -> "Not implemented"))
                        .fieldOf("inputs").forGetter(MeatLibCodecs::notImplemented),
                RecipeOutputs.FLUID.fieldOf("output").forGetter(MeatLibCodecs::notImplemented),
                Codec.INT.fieldOf("process_time").forGetter(MeatLibCodecs::notImplemented)
        ).apply(instance, (i, o, t) -> id ->
                new BioreactorRecipe(id, i.getFirst(), i.getSecond(), o, t)));

        @Override
        public Codec<RecipeFunction<BioreactorRecipe>> codec()
        {
            return codec;
        }

        private static Pair<List<RegistryRecipeInput<Item>>, List<RegistryRecipeInput<Fluid>>> sortInputs(List<RecipeInput<?, ?>> inputs)
        {
            List<RegistryRecipeInput<Item>> itemInputs = new ArrayList<>();
            List<RegistryRecipeInput<Fluid>> fluidInputs = new ArrayList<>();

            for (var recipeInput : inputs)
            {
                if (recipeInput instanceof RegistryRecipeInput<?> rri)
                {
                    if (recipeInput.getType() == Ingredients.ITEM)
                        itemInputs.add((RegistryRecipeInput<Item>) rri);
                    else if (recipeInput.getType() == Ingredients.FLUID)
                        fluidInputs.add((RegistryRecipeInput<Fluid>) rri);
                }
            }

            return new Pair<>(itemInputs, fluidInputs);
        }

        @Override
        public BioreactorRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
        {
            List<RegistryRecipeInput<Item>> itemInputs = PacketBufUtil.readList(buf, RegistryRecipeInput::fromBuffer);
            List<RegistryRecipeInput<Fluid>> fluidInputs = PacketBufUtil.readList(buf, RegistryRecipeInput::fromBuffer);

            RecipeOutput<Fluid> output = RecipeOutputImpl.read(BuiltInRegistries.FLUID, buf);

            int processTime = buf.readVarInt();

            return new BioreactorRecipe(id, itemInputs, fluidInputs, output, processTime);
        }

        @Override
        public void write(FriendlyByteBuf buf, BioreactorRecipe recipe)
        {
            PacketBufUtil.writeList(buf, recipe.fluidInputs, (b, i) -> i.write(b));
            PacketBufUtil.writeList(buf, recipe.itemInputs, (b, i) -> i.write(b));

            recipe.output.write(buf);

            buf.writeVarInt(recipe.processTime);
        }
    }
}
