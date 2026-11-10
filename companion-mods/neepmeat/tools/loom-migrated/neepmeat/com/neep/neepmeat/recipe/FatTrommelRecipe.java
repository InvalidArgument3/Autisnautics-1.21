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
import com.neep.meatlib.recipe.MeatRecipeSerialiser;
import com.neep.meatlib.recipe.MeatRecipeType;
import com.neep.neepmeat.api.processing.OreFatRegistry;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.small_trommel.TrommelRecipe;
import com.neep.neepmeat.machine.small_trommel.TrommelStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

@SuppressWarnings("UnstableApiUsage")
public class FatTrommelRecipe implements TrommelRecipe
{
    protected final ResourceLocation id;
    private final RandomSource random = RandomSource.create();

    public FatTrommelRecipe(ResourceLocation id)
    {
        this.id = id;
    }

    @Override
    public boolean matches(TrommelStorage inventory)
    {
        SingleVariantStorage<FluidVariant> storage = inventory.input();
        FluidVariant resource = storage.getResource();
        return resource.isOf(NMFluids.STILL_DIRTY_ORE_FAT) && OreFatRegistry.getFromVariant(storage.getResource()) != null;
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    public MeatRecipeType<?> getType()
    {
        return NMrecipeTypes.FAT_TROMMEL;
    }

    @Override
    public MeatRecipeSerialiser<?> getSerializer()
    {
        return NMrecipeTypes.FAT_TROMMEL_SERIALIZER;
    }

    @Override
    public boolean takeInputs(TrommelStorage storage, TransactionContext transaction)
    {
        return true;
    }

    @Override
    public boolean ejectOutputs(TrommelStorage context, TransactionContext transaction)
    {
        try (Transaction inner = transaction.openNested())
        {
            SingleVariantStorage<FluidVariant> fluidStorage = context.input();
            FluidVariant inputVariant = fluidStorage.getResource();

            long ex2 = fluidStorage.extract(inputVariant, INPUT_AMOUNT, inner);
            if (ex2 != INPUT_AMOUNT)
                return false;

            OreFatRegistry.Entry entry = OreFatRegistry.getFromVariant(inputVariant);
            if (entry == null)
                return false;

            float trommelYield = entry.trommelYield();
            float fractional = trommelYield % 1.0f;
            int ingots = (int) (Math.floor(trommelYield) + (random.nextFloat() < fractional ? 1 : 0));
            long outputAmount = (long) INPUT_AMOUNT * ingots;

            FluidVariant outputVariant = entry.getClean();

            long inserted = context.output().insert(outputVariant, outputAmount, inner);

            if (inserted == outputAmount)
            {
                inner.commit();
                return true;
            }
        }
        return false;
    }

    public static class Serializer implements MeatRecipeSerialiser<FatTrommelRecipe>
    {
        public Serializer()
        {
        }

        @Override
        public Codec<RecipeFunction<FatTrommelRecipe>> codec()
        {
            return Codec.unit(FatTrommelRecipe::new);
        }

        @Override
        public FatTrommelRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
        {
            return new FatTrommelRecipe(id);
        }

        @Override
        public void write(FriendlyByteBuf buf, FatTrommelRecipe recipe)
        {
        }
    }
}
