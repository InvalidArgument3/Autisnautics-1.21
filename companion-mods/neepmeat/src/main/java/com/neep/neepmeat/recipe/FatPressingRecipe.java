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
import com.neep.meatlib.recipe.ingredient.RegistryRecipeInput;
import com.neep.meatlib.recipe.ingredient.RecipeInputs;
import com.neep.meatlib.util.MeatLibCodecs;
import com.neep.neepmeat.api.processing.OreFatRegistry;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.casting_basin.CastingBasinStorage;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;

@SuppressWarnings("UnstableApiUsage")
public class FatPressingRecipe extends AbstractPressingRecipe<CastingBasinStorage>
{
    protected ResourceLocation id;
    protected RegistryRecipeInput<Fluid> fluidInput;

    public FatPressingRecipe(ResourceLocation id, RegistryRecipeInput<Fluid> fluidInput)
    {
        this.fluidInput = fluidInput;
        this.id = id;
    }

    @Override
    public boolean matches(CastingBasinStorage inventory)
    {
        WritableSingleFluidStorage storage = inventory.fluid(null);
        return OreFatRegistry.getFromVariant(storage.getResource()) != null
                && fluidInput.amount() == storage.getAmount();
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
        return NMrecipeTypes.FAT_PRESSING_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType()
    {
        return NMrecipeTypes.FAT_PRESSING;
    }

    public boolean takeInputs(CastingBasinStorage storage, TransactionContext transaction)
    {
        try (Transaction inner = transaction.openNested())
        {
            WritableSingleFluidStorage fluidStorage = storage.fluid(null);
            FluidVariant fluidVariant = fluidStorage.getResource();

            // Ensure that storage contents still match the recipe
            OreFatRegistry.Entry entry = OreFatRegistry.getFromVariant(fluidVariant);
            if (entry == null)
            {
                throw new IllegalStateException("Storage contents do not conform to recipe");
            }

            storage.unlock();
            long ex2 = fluidStorage.extract(fluidStorage.getResource(), fluidInput.amount(), inner);
            storage.lock();

            if (ex2 != fluidInput.amount())
            {
                inner.abort();
                return false;
            }

            // Might need to formalise this
            long insertAmount = 1;
            long transferred = storage.item(null).insert(entry.result(), insertAmount, inner);

            if (transferred == insertAmount)
            {
                inner.commit();
                return true;
            }
            inner.abort();
        }
        return false;
    }

    public static class Serializer implements MeatRecipeSerialiser<FatPressingRecipe>
    {
        private final Codec<RecipeFunction<FatPressingRecipe>> codec = RecordCodecBuilder.create(instance -> instance.group(
                RecipeInputs.FLUID.codec().fieldOf("fluid_input").forGetter(MeatLibCodecs::notImplemented)
        ).apply(instance, (i) -> id -> new FatPressingRecipe(id, i)));

        public Serializer()
        {
        }

        @Override
        public Codec<RecipeFunction<FatPressingRecipe>> codec()
        {
            return codec;
        }

        @Override
        public FatPressingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
        {
            RegistryRecipeInput<Fluid> fluidInput = RegistryRecipeInput.fromBuffer(buf);

            return new FatPressingRecipe(id, fluidInput);
        }

        @Override
        public void write(FriendlyByteBuf buf, FatPressingRecipe recipe)
        {
            recipe.fluidInput.write(buf);
        }

        @FunctionalInterface
        public interface RecipeFactory<T extends FatPressingRecipe>
        {
            T create(ResourceLocation var1, RegistryRecipeInput<Fluid> in);
        }
    }
}
