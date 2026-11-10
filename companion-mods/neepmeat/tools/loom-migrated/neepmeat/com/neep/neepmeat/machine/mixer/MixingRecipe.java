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

package com.neep.neepmeat.machine.mixer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neep.meatlib.recipe.MeatRecipeSerialiser;
import com.neep.meatlib.recipe.MeatlibRecipe;
import com.neep.meatlib.recipe.ingredient.*;
import com.neep.meatlib.util.MeatLibCodecs;
import com.neep.neepmeat.init.NMrecipeTypes;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

@SuppressWarnings("UnstableApiUsage")
public class MixingRecipe implements MeatlibRecipe<MixerStorage>
{
    protected ResourceLocation id;
    protected RegistryRecipeInput<Item> itemInput;
    protected RegistryRecipeInput<Fluid> fluidInput1;
    protected RegistryRecipeInput<Fluid> fluidInput2;
    protected RecipeOutput<Fluid> fluidOutput;
    protected int processTime;

    public MixingRecipe(ResourceLocation id, RegistryRecipeInput<Item> itemInput, RegistryRecipeInput<Fluid> fluidInput1, RegistryRecipeInput<Fluid> fluidInput2, RecipeOutput<Fluid> fluidOutput, int processTime)
    {
        this.itemInput = itemInput;
        this.fluidInput1 = fluidInput1;
        this.fluidInput2 = fluidInput2;
        this.fluidOutput = fluidOutput;
        this.processTime = processTime;
        this.id = id;
    }

    public RegistryRecipeInput<Item> getItemIngredient()
    {
        return itemInput;
    }

    public List<RegistryRecipeInput<Fluid>> getFluidInputs()
    {
        return List.of(fluidInput1, fluidInput2);
    }

    public RecipeOutput<Fluid> getFluidOutput()
    {
        return fluidOutput;
    }

    public int getProcessTime()
    {
        return processTime;
    }

    @Override
    public boolean matches(MixerStorage inventory)
    {
        Transaction transaction = Transaction.openOuter();
        List<RegistryRecipeInput<Fluid>> queue = new LinkedList<>(List.of(fluidInput1, fluidInput2));

        ListIterator<RegistryRecipeInput<Fluid>> it = queue.listIterator();
        var parentStorages = inventory.getInputStorages();
        while (it.hasNext())
        {
            RegistryRecipeInput<Fluid> ingredient = it.next();
            if (ingredient.isEmpty())
            {
                it.remove();
                continue;
            }
            for (StorageView<FluidVariant> view : parentStorages)
            {
                if (ingredient.test(view))
                {
                    it.remove();
                    break;
                }
            }
        }
        transaction.abort();
        return queue.isEmpty() && itemInput.test(inventory.getItemInput());
    }

    @Override
    public boolean takeInputs(MixerStorage inventory, TransactionContext transactionContext)
    {
        try (Transaction inner = transactionContext.openNested())
        {
            List<Storage<FluidVariant>> inputList = inventory.parent.getAdjacentStorages();
            Storage<FluidVariant> fluidStorage = inventory.getInputStorages();

            if (!inputList.isEmpty())
            {
                FluidVariant fluid1 = fluidInput1.isEmpty() ? FluidVariant.blank() : fluidInput1.getFirstMatching(fluidStorage, FluidVariant::of).orElse(null);
                FluidVariant fluid2 = fluidInput2.isEmpty() ? FluidVariant.blank() : fluidInput2.getFirstMatching(fluidStorage, FluidVariant::of).orElse(null);
                ItemVariant item = itemInput.isEmpty() ? ItemVariant.of(Items.AIR) : itemInput.getFirstMatching((StorageView<ItemVariant>) inventory.getItemInput(), ItemVariant::of).orElse(null);

                if (fluid1 == null || fluid2 == null || item == null)
                    return false;

                long ext1 = fluidStorage.extract(fluid1, fluidInput1.amount(), inner);

                // Do not attempt to extract blank variant
                long ext2 = fluid2.isBlank() ? fluidInput2.amount() : fluidStorage.extract(fluid2, fluidInput2.amount(), inner);
                long ext3 = itemInput.isEmpty() ? itemInput.amount() : inventory.getItemInput().extract(item, itemInput.amount(), inner);

                if (ext1 == fluidInput1.amount() && ext2 == fluidInput2.amount() && ext3 == itemInput.amount())
                {
                    inner.commit();
                    inventory.displayInput1 = fluid1;
                    inventory.displayInput2 = fluid2;
                    return true;
                }
            }
            inner.abort();
        }
        return false;
    }

    @Override
    public boolean ejectOutputs(MixerStorage inventory, TransactionContext transaction)
    {
        try (Transaction inner = transaction.openNested())
        {
            Storage<FluidVariant> output = inventory.getFluidOutput();
            if (fluidOutput.insertInto(output, FluidVariant::of, transaction))
            {
                inner.commit();
                return true;
            }
            inner.abort();
        }
        return false;
    }
    
    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    public MeatRecipeSerialiser<?> getSerializer()
    {
        return NMrecipeTypes.MIXING_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType()
    {
        return NMrecipeTypes.MIXING;
    }

    public static class MixerSerializer implements MeatRecipeSerialiser<MixingRecipe>
    {
        private final Codec<RecipeFunction<MixingRecipe>> codec;

        public MixerSerializer()
        {
            codec = RecordCodecBuilder.create(instance -> instance.group(
                    RecipeInputs.ITEM.codec().fieldOf("item").forGetter(MeatLibCodecs::notImplemented),
                    RecipeInputs.FLUID.codec().fieldOf("fluid1").forGetter(MeatLibCodecs::notImplemented),
                    RecipeInputs.FLUID.codec().fieldOf("fluid2").forGetter(MeatLibCodecs::notImplemented),
                    RecipeOutputs.FLUID.fieldOf("output").forGetter(MeatLibCodecs::notImplemented),
                    Codec.INT.fieldOf("processtime").forGetter(MeatLibCodecs::notImplemented)
            ).apply(instance, (i, f1, f2, o, t) ->
                    id -> new MixingRecipe(id, i, f1, f2, o, t)));
        }

        @Override
        public Codec<RecipeFunction<MixingRecipe>> codec()
        {
            return codec;
        }

        @Override
        public MixingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
        {
            RegistryRecipeInput<Item> ingredient = RegistryRecipeInput.fromBuffer(buf);
            RegistryRecipeInput<Fluid> fluidInput1 = RegistryRecipeInput.fromBuffer(buf);
            RegistryRecipeInput<Fluid> fluidInput2 = RegistryRecipeInput.fromBuffer(buf);
            RecipeOutputImpl<Fluid> fluidOutput = RecipeOutputImpl.read(BuiltInRegistries.FLUID, buf);
            int time = buf.readVarInt();

            return new MixingRecipe(id, ingredient, fluidInput1, fluidInput2, fluidOutput, time);
        }

        @Override
        public void write(FriendlyByteBuf buf, MixingRecipe recipe)
        {
            recipe.itemInput.write(buf);
            recipe.fluidInput1.write(buf);
            recipe.fluidInput2.write(buf);
            recipe.fluidOutput.write(buf);
            buf.writeVarInt(recipe.processTime);
        }

        @FunctionalInterface
        public interface RecipeFactory<T extends MixingRecipe>
        {
            T create(ResourceLocation var1, RegistryRecipeInput<Item> var3, RegistryRecipeInput<Fluid> f1, RegistryRecipeInput<Fluid> f2, RecipeOutputImpl<Fluid> out, int time);
        }
    }

}
