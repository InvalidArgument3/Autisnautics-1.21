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

package com.neep.neepmeat.api.processing.ball_mill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.ParamCodecs;
import com.neep.meatlib.api.network.RecordParamCodec;
import com.neep.meatlib.recipe.MeatRecipeSerialiser;
import com.neep.meatlib.recipe.ingredient.RecipeInputs;
import com.neep.meatlib.recipe.ingredient.RecipeOutput;
import com.neep.meatlib.recipe.ingredient.RecipeOutputs;
import com.neep.meatlib.recipe.ingredient.RegistryRecipeInput;
import com.neep.meatlib.util.MeatLibCodecs;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;
import java.util.List;
import java.util.Optional;

public record DistinctBallMillRecipe(ResourceLocation id, RegistryRecipeInput<Item> input, List<RecipeOutput<Item>> outputs,
                                     int processTime) implements BallMillRecipe
{
    public static final ParamCodec<DistinctBallMillRecipe> PARAM_CODEC = ParamCodecs.ofRecipe(DistinctBallMillRecipe.class, NMrecipeTypes.BALL_MILL_SERIALISER);

    @Override
    public boolean matches(BallMillContext context)
    {
        return input.test(context.getInput());
    }

    @Override
    public boolean takeInputs(BallMillContext context, TransactionContext transaction)
    {
        try (Transaction inner = transaction.openNested())
        {
            Optional<ItemVariant> item = input.getFirstMatching(context.getInput(), ItemVariant::of);
            if (item.isEmpty())
            {
                inner.abort();
                return false;
            }

            long extracted = context.getInput().extract(item.get(), input.amount(), inner);

            if (extracted == input.amount())
            {
                inner.commit();
                return true;
            }
            inner.abort();
        }

        return false;
    }

    @Override
    public boolean ejectOutputs(BallMillContext context, TransactionContext transaction)
    {
        try (Transaction inner = transaction.openNested())
        {
            boolean success = true;
            for (var output : outputs)
            {
                success &= output.insertInto(context.getOutput(), ItemVariant::of, inner);
            }

            if (success)
            {
                inner.commit();
                return true;
            }
            inner.abort();
        }
        return false;
    }

    @Override
    public MeatRecipeSerialiser<?> getSerializer()
    {
        return NMrecipeTypes.BALL_MILL_SERIALISER;
    }

    @Override
    public RecipeType<?> getType()
    {
        return NMrecipeTypes.BALL_MILL;
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    public int getTime()
    {
        return processTime;
    }

    @Override
    public RegistryRecipeInput<Item> getInput()
    {
        return input;
    }

    @Override
    public List<RecipeOutput<Item>> getOutputs()
    {
        return outputs;
    }

    // Responsible for loading normal JSON recipes
    public static class Serialiser implements MeatRecipeSerialiser<DistinctBallMillRecipe>
    {
        private final Codec<RecipeFunction<DistinctBallMillRecipe>> codec = RecordCodecBuilder.create(instance -> instance.group(
                RecipeInputs.ITEM.codec().fieldOf("input").forGetter(MeatLibCodecs::notImplemented),
                Codec.list(RecipeOutputs.ITEM).fieldOf("output").forGetter(MeatLibCodecs::notImplemented),
                Codec.INT.optionalFieldOf("process_time", 40).forGetter(MeatLibCodecs::notImplemented)
        ).apply(instance, (input, outputs, time) -> id -> new DistinctBallMillRecipe(id, input, outputs, time)));

        private final ParamCodec<DistinctBallMillRecipe> paramCodec = RecordParamCodec.builder(DistinctBallMillRecipe.class)
                .param(ParamCodecs.IDENTIFIER)
                .param(RecipeInputs.ITEM.paramCodec())
                .param(ParamCodec.list(RecipeOutputs.ITEM_PARAM))
                .param(ParamCodec.INT)
                .build();

        @Override
        public Codec<RecipeFunction<DistinctBallMillRecipe>> codec()
        {
            return codec;
        }

        @Override
        public void write(FriendlyByteBuf buf, DistinctBallMillRecipe recipe)
        {
            paramCodec.encode(recipe, buf);
        }

        @Override
        public DistinctBallMillRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
        {
            return paramCodec.decode(buf);
        }
    }
}
