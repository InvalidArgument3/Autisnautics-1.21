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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neep.meatlib.recipe.MeatRecipeSerialiser;
import com.neep.meatlib.recipe.MeatRecipeType;
import com.neep.meatlib.util.MeatLibCodecs;
import com.neep.meatlib.recipe.ingredient.*;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.small_trommel.TrommelRecipe;
import com.neep.neepmeat.machine.small_trommel.TrommelStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class NormalTrommelRecipe implements TrommelRecipe
{
    protected final ResourceLocation id;
    protected final RegistryRecipeInput<Fluid> fluidInput;
    protected final RecipeOutput<Fluid> fluidOutput;
    protected final RecipeOutput<Item> itemOutput;

    public NormalTrommelRecipe(ResourceLocation id, RegistryRecipeInput<Fluid> fluidInput, RecipeOutput<Fluid> fluidOutput, @Nullable RecipeOutput<Item> itemOutput)
    {
        this.fluidInput = fluidInput;
        this.fluidOutput = fluidOutput;
        this.itemOutput = itemOutput;
        this.id = id;
    }

    @Override
    public boolean matches(TrommelStorage inventory)
    {
        SingleVariantStorage<FluidVariant> storage = inventory.input();
        return fluidInput.test(storage) && fluidInput.amount() <= storage.getAmount();
    }

    public RegistryRecipeInput<Fluid> getFluidInput()
    {
        return fluidInput;
    }

    public RecipeOutput<Fluid> getFluidOutput()
    {
        return fluidOutput;
    }

    public RecipeOutput<Item> getAuxOutput()
    {
        return itemOutput;
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    public MeatRecipeType<?> getType()
    {
        return NMrecipeTypes.TROMMEL;
    }

    @Override
    public MeatRecipeSerialiser<?> getSerializer()
    {
        return NMrecipeTypes.TROMMEL_SERIALIZER;
    }

    @Override
    public boolean takeInputs(TrommelStorage storage, TransactionContext transaction)
    {
        try (Transaction inner = transaction.openNested())
        {
            Storage<FluidVariant> fluidStorage = storage.input();
            Optional<FluidVariant> fluid = fluidInput.getFirstMatching(fluidStorage, FluidVariant::of);
            if (fluid.isEmpty())
                return false;

            long ex2 = fluidStorage.extract(fluid.get(), fluidInput.amount(), inner);
            if (ex2 == fluidInput.amount())
            {
                inner.commit();
                return true;
            }
            inner.abort();
        }
        return false;
    }

    @Override
    public boolean ejectOutputs(TrommelStorage context, TransactionContext transaction)
    {
        try (Transaction inner = transaction.openNested())
        {
            boolean bl1 = fluidOutput.insertInto(context.output(), FluidVariant::of, inner);

            if (itemOutput != null)
            {
                itemOutput.insertInto(context.itemOutput(), ItemVariant::of, inner);
            }

            if (bl1)
            {
                inner.commit();
                return true;
            }
            inner.abort();
        }
        return false;
    }

    public static class Serializer implements MeatRecipeSerialiser<NormalTrommelRecipe>
    {
        private final RecipeFactory<NormalTrommelRecipe> factory;
        private final Codec<RecipeFunction<NormalTrommelRecipe>> codec;

        public Serializer(RecipeFactory<NormalTrommelRecipe> recipeFactory)
        {
            this.factory = recipeFactory;

            this.codec = RecordCodecBuilder.create(instance -> instance.group(
                    RecipeInputs.FLUID.codec().fieldOf("input").forGetter(MeatLibCodecs::notImplemented),
                    RecipeOutputs.FLUID.fieldOf("output").forGetter(MeatLibCodecs::notImplemented),
                    RecipeOutputs.ITEM.optionalFieldOf("aux_output").forGetter(MeatLibCodecs::notImplemented)
            ).apply(instance, (i, o1, o2) -> id ->
                    new NormalTrommelRecipe(id, i, o1, o2.orElse(null))));
        }

        @Override
        public Codec<RecipeFunction<NormalTrommelRecipe>> codec()
        {
            return codec;
        }

        @Override
        public NormalTrommelRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
        {
            RegistryRecipeInput<Fluid> fluidInput = RegistryRecipeInput.fromBuffer(buf);
            RecipeOutputImpl<Fluid> fluidOutput = RecipeOutputImpl.read(BuiltInRegistries.FLUID, buf);
            Optional<RecipeOutputImpl<Item>> itemOutput = buf.readOptional(b -> RecipeOutputImpl.read(BuiltInRegistries.ITEM, b));

            return this.factory.create(id, fluidInput, fluidOutput, itemOutput.orElse(null));
        }

        @Override
        public void write(FriendlyByteBuf buf, NormalTrommelRecipe recipe)
        {
            recipe.fluidInput.write(buf);
            recipe.fluidOutput.write(buf);
            buf.writeOptional(Optional.ofNullable(recipe.itemOutput), (b, o) -> o.write(b));
        }

        @FunctionalInterface
        public interface RecipeFactory<T extends NormalTrommelRecipe>
        {
            T create(ResourceLocation id, RegistryRecipeInput<Fluid> in, RecipeOutput<Fluid> out, RecipeOutput<Item> itemOut);
        }
    }
}
