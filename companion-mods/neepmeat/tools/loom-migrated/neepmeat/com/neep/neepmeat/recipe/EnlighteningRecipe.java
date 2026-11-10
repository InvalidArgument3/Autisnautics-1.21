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
import com.neep.meatlib.recipe.MeatlibRecipe;
import com.neep.meatlib.util.MeatLibCodecs;
import com.neep.meatlib.recipe.ingredient.*;
import com.neep.neepmeat.api.DataResource;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.integrator.Integrator;
import com.neep.neepmeat.machine.pedestal.PedestalBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;

@SuppressWarnings("UnstableApiUsage")
public class EnlighteningRecipe implements MeatlibRecipe<PedestalBlockEntity.RecipeBehaviour>
{
    protected ResourceLocation id;
    protected RegistryRecipeInput<Item> itemInput;
    protected RecipeOutput<Item> itemOutput;
    protected long data;

    public EnlighteningRecipe(ResourceLocation id, RegistryRecipeInput<Item> fluidInput, RecipeOutput<Item> itemOutput, long data)
    {
        this.itemInput = fluidInput;
        this.itemOutput = itemOutput;
        this.data = data;
        this.id = id;
    }

    @Override
    public boolean matches(PedestalBlockEntity.RecipeBehaviour inventory)
    {
        return itemInput.test(inventory.getStorage());
    }

    public RegistryRecipeInput<Item> getItemInput()
    {
        return itemInput;
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    public MeatRecipeSerialiser<?> getSerializer()
    {
        return NMrecipeTypes.ENLIGHTENING_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType()
    {
        return NMrecipeTypes.ENLIGHTENING;
    }

    @Override
    public boolean takeInputs(PedestalBlockEntity.RecipeBehaviour context, TransactionContext transaction) { return true; }

    @Override
    public boolean ejectOutputs(PedestalBlockEntity.RecipeBehaviour context, TransactionContext transaction)
    {
        try (Transaction inner = transaction.openNested())
        {
            WritableStackStorage stackStorage = context.getStorage();
//
//            // Ensure that storage contents still match the recipe
            long amount = stackStorage.getAmount();
            Item input = itemInput.getFirstMatching(stackStorage).orElse(null);
            Integrator integrator = context.getIntegrator();

            if (integrator == null)
                return false;

            float storedData = integrator.getData(DataResource.NORMAL);
            if (input == null || storedData < data)
            {
                return false;
            }

            long ext = stackStorage.extract(stackStorage.getResource(), amount * itemInput.amount(), inner);
            float dataExt = integrator.extract(DataResource.NORMAL, amount * data, transaction);

            if (ext != amount * itemInput.amount() || dataExt != amount * data)
            {
                inner.abort();
                return false;
            }

            if (itemOutput.insertInto(stackStorage, ItemVariant::of, inner))
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

    public long getData()
    {
        return data;
    }

    public static class Serializer implements MeatRecipeSerialiser<EnlighteningRecipe>
    {
        private final Codec<RecipeFunction<EnlighteningRecipe>> codec = RecordCodecBuilder.create(instance -> instance.group(
                RecipeInputs.ITEM.codec().fieldOf("item_input").forGetter(MeatLibCodecs::notImplemented),
                RecipeOutputs.ITEM.fieldOf("output").forGetter(MeatLibCodecs::notImplemented),
                Codec.INT.fieldOf("data").forGetter(MeatLibCodecs::notImplemented)
        ).apply(instance, (i, o, d) -> id -> new EnlighteningRecipe(id, i, o, d)));

        public Serializer()
        {
        }

        @Override
        public Codec<RecipeFunction<EnlighteningRecipe>> codec()
        {
            return codec;
        }

        @Override
        public EnlighteningRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
        {
            RegistryRecipeInput<Item> itemInput = RegistryRecipeInput.fromBuffer(buf);
            RecipeOutputImpl<Item> itemOutput = RecipeOutputImpl.read(BuiltInRegistries.ITEM, buf);
            long data = buf.readVarLong();

            return new EnlighteningRecipe(id, itemInput, itemOutput, data);
        }

        @Override
        public void write(FriendlyByteBuf buf, EnlighteningRecipe recipe)
        {
            recipe.itemInput.write(buf);
            recipe.itemOutput.write(buf);
            buf.writeVarLong(recipe.data);
        }

        @FunctionalInterface
        public interface RecipeFactory<T extends EnlighteningRecipe>
        {
            T create(ResourceLocation var1, RegistryRecipeInput<Item> in, RecipeOutputImpl<Item> out, long data);
        }
    }
}
