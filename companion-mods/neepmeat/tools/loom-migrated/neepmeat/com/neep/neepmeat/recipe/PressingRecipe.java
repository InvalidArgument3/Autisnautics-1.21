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
import com.neep.meatlib.util.MeatLibCodecs;
import com.neep.meatlib.recipe.ingredient.*;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.casting_basin.CastingBasinStorage;
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

@SuppressWarnings("UnstableApiUsage")
public class PressingRecipe extends AbstractPressingRecipe<CastingBasinStorage>
{
    protected ResourceLocation id;
    protected RegistryRecipeInput<Fluid> fluidInput;
    protected RecipeOutput<Item> itemOutput;

    public PressingRecipe(ResourceLocation id, RegistryRecipeInput<Fluid> fluidInput, RecipeOutput<Item> itemOutput)
    {
        this.fluidInput = fluidInput;
        this.itemOutput = itemOutput;
        this.id = id;
    }

    @Override
    public boolean matches(CastingBasinStorage inventory)
    {
        WritableSingleFluidStorage storage = inventory.fluid(null);
        return fluidInput.test(storage) && storage.getAmount() == fluidInput.amount();
    }

    public RegistryRecipeInput<Fluid> getFluidInput()
    {
        return fluidInput;
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    public MeatRecipeSerialiser<?> getSerializer()
    {
        return NMrecipeTypes.PRESSING_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType()
    {
        return NMrecipeTypes.PRESSING;
    }

    public boolean takeInputs(CastingBasinStorage storage, TransactionContext transaction)
    {
        try (Transaction inner = transaction.openNested())
        {
            WritableSingleFluidStorage fluidStorage = storage.fluid(null);
            FluidVariant fluidVariant = fluidStorage.getResource();
            Fluid fluid = fluidInput.getFirstMatching(fluidStorage).orElse(null);

            // Ensure that storage contents still match the recipe
            if (fluid == null)
            {
                return false;
            }

            storage.unlock();
            long ex2 = fluidStorage.extract(fluidStorage.getResource(), fluidInput.amount(), inner);
            storage.lock();

            if (ex2 != fluidInput.amount())
            {
                inner.abort();
                return false;
            }

            boolean transferred = itemOutput.insertInto(storage.item(null), ItemVariant::of, transaction);

            if (transferred)
            {
                inner.commit();
                return true;
            }
            inner.abort();
        }
        return false;
    }

    public RecipeOutput<Item> getItemOutput()
    {
        return itemOutput;
    }

    public static class Serializer implements MeatRecipeSerialiser<PressingRecipe>
    {
        private final Codec<RecipeFunction<PressingRecipe>> codec = RecordCodecBuilder.create(instance -> instance.group(
                RecipeInputs.FLUID.codec().fieldOf("fluid_input").forGetter(MeatLibCodecs::notImplemented),
                RecipeOutputs.ITEM.fieldOf("output").forGetter(MeatLibCodecs::notImplemented)
        ).apply(instance, (i, o) -> id -> new PressingRecipe(id, i, o)));

        public Serializer()
        {
        }

        @Override
        public Codec<RecipeFunction<PressingRecipe>> codec()
        {
            return codec;
        }

        @Override
        public PressingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
        {
            RegistryRecipeInput<Fluid> fluidInput = RegistryRecipeInput.fromBuffer(buf);
            RecipeOutputImpl<Item> itemOutput = RecipeOutputImpl.read(BuiltInRegistries.ITEM, buf);

            return new PressingRecipe(id, fluidInput, itemOutput);
        }

        @Override
        public void write(FriendlyByteBuf buf, PressingRecipe recipe)
        {
            recipe.fluidInput.write(buf);
            recipe.itemOutput.write(buf);
        }

        @FunctionalInterface
        public interface RecipeFactory<T extends PressingRecipe>
        {
            T create(ResourceLocation var1, RegistryRecipeInput<Fluid> in, RecipeOutputImpl<Item> out);
        }
    }
}
