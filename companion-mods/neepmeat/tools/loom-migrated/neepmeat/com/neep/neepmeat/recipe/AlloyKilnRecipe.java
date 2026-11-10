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
import com.neep.meatlib.recipe.MeatlibRecipe;
import com.neep.meatlib.util.MeatLibCodecs;
import com.neep.meatlib.recipe.ingredient.*;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.alloy_kiln.AlloyKilnStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class AlloyKilnRecipe implements MeatlibRecipe<AlloyKilnStorage>
{
    protected ResourceLocation id;
    protected RegistryRecipeInput<Item> itemInput1;
    protected RegistryRecipeInput<Item> itemInput2;
    protected RecipeOutput<Item> itemOutput;
    protected int processTime;

    public AlloyKilnRecipe(ResourceLocation id, RegistryRecipeInput<Item> itemInput1, RegistryRecipeInput<Item> itemInput2, RecipeOutput<Item> itemOutput, int processTime)
    {
        this.itemInput1 = itemInput1;
        this.itemInput2 = itemInput2;
        this.itemOutput = itemOutput;
        this.processTime = processTime;
        this.id = id;
    }

    @Override
    public boolean matches(AlloyKilnStorage context)
    {
        List<SingleSlotStorage<ItemVariant>> slots = List.of(
                context.getSlot(AlloyKilnStorage.INPUT_1),
                context.getSlot(AlloyKilnStorage.INPUT_2));

        List<RegistryRecipeInput<Item>> queue = new LinkedList<>(List.of(itemInput1, itemInput2));

        ListIterator<RegistryRecipeInput<Item>> it = queue.listIterator();
        while (it.hasNext())
        {
            RegistryRecipeInput<Item> ingredient = it.next();
            for (StorageView<ItemVariant> view : slots)
            {
                if (ingredient.test(view))
                {
                    it.remove();
                    break;
                }
            }
        }
        return queue.size() == 0;
    }

    public RegistryRecipeInput<Item> getItemInput1()
    {
        return itemInput1;
    }

    public RegistryRecipeInput<Item> getItemInput2()
    {
        return itemInput2;
    }

    public RecipeOutput<Item> getItemOutput()
    {
        return itemOutput;
    }

    public int getProcessTime()
    {
        return processTime;
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    public MeatRecipeType<?> getType()
    {
        return NMrecipeTypes.ALLOY_SMELTING;
    }

    @Override
    public MeatRecipeSerialiser<?> getSerializer()
    {
        return NMrecipeTypes.ALLOY_KILN_SERIALIZER;
    }

    public int getTime()
    {
        return processTime;
    }

    public boolean takeInputs(AlloyKilnStorage storage, TransactionContext transaction)
    {
        try (Transaction inner = transaction.openNested())
        {
            Storage<ItemVariant> inputStorage = storage.getInputStorage();

            Optional<ItemVariant> item1 = itemInput1.getFirstMatching(inputStorage, ItemVariant::of);
            Optional<ItemVariant> item2 = itemInput2.getFirstMatching(inputStorage, ItemVariant::of);

            if (item1.isEmpty() || item2.isEmpty())
            {
                inner.abort();
                return false;
            }

            long extracted1 = inputStorage.extract(item1.get(), itemInput1.amount(), transaction);
            long extracted2 = inputStorage.extract(item2.get(), itemInput2.amount(), transaction);
            if (extracted1 == itemInput1.amount() && extracted2 == itemInput2.amount())
            {
                inner.commit();
                storage.markDirty();
                return true;
            }
            inner.abort();
        }
        return false;
    }

    @Override
    public boolean ejectOutputs(AlloyKilnStorage context, TransactionContext transaction)
    {
        return false;
    }

    public boolean ejectOutput(AlloyKilnStorage storage, TransactionContext transaction)
    {
        try (Transaction inner = transaction.openNested())
        {
            boolean bl1 = itemOutput.insertInto(storage.getOutputStorage(), ItemVariant::of, inner);

            if (bl1)
            {
                inner.commit();
                storage.markDirty();
                return true;
            }
            inner.abort();
        }
        return false;
    }

    public static class Serializer implements MeatRecipeSerialiser<AlloyKilnRecipe>
    {
        private final Codec<RecipeFunction<AlloyKilnRecipe>> codec;

        RecipeFactory<AlloyKilnRecipe> factory;
        int processTIme;

        public Serializer(RecipeFactory<AlloyKilnRecipe> recipeFactory, int processTime)
        {
            this.factory = recipeFactory;
            this.processTIme = processTime;

            this.codec = RecordCodecBuilder.create(instance -> instance.group(
                    RecipeInputs.ITEM.codec().fieldOf("input1").forGetter(MeatLibCodecs::notImplemented),
                    RecipeInputs.ITEM.codec().fieldOf("input2").forGetter(MeatLibCodecs::notImplemented),
                    RecipeOutputs.ITEM.fieldOf("output").forGetter(MeatLibCodecs::notImplemented),
                    Codec.INT.optionalFieldOf("processtime", processTime).forGetter(MeatLibCodecs::notImplemented)
                    ).apply(instance, (i1, i2, o, t) -> id -> factory.create(id, i1, i2, o, t)));
        }

        @Override
        public Codec<RecipeFunction<AlloyKilnRecipe>> codec()
        {
            return codec;
        }

        @Override
        public AlloyKilnRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
        {
            RegistryRecipeInput<Item> itemInput1 = RegistryRecipeInput.fromBuffer(buf);
            RegistryRecipeInput<Item> itemInput2 = RegistryRecipeInput.fromBuffer(buf);
            RecipeOutputImpl<Item> itemOutput = RecipeOutputImpl.read(BuiltInRegistries.ITEM, buf);

            int time = buf.readVarInt();

            return this.factory.create(id, itemInput1, itemInput2, itemOutput, time);
        }

        @Override
        public void write(FriendlyByteBuf buf, AlloyKilnRecipe recipe)
        {
            recipe.itemInput1.write(buf);
            recipe.itemInput2.write(buf);
            recipe.itemOutput.write(buf);

            buf.writeVarInt(recipe.processTime);
        }

        @FunctionalInterface
        public interface RecipeFactory<T extends AlloyKilnRecipe>
        {
            T create(ResourceLocation var1, RegistryRecipeInput<Item> in1, RegistryRecipeInput<Item> in2, RecipeOutput<Item> out, int time);
        }
    }
}
