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
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.crucible.CrucibleStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class RenderingRecipe implements RenderingBasinRecipe
{
    protected ResourceLocation id;
    protected RegistryRecipeInput<Item> itemInput;
    protected RegistryRecipeInput<Fluid> fluidInput;
    protected RecipeOutput<Fluid> fluidOutput;

    public RenderingRecipe(ResourceLocation id, RegistryRecipeInput<Item> itemInput, RegistryRecipeInput<Fluid> fluidInput, RecipeOutput<Fluid> fluidOutput)
    {
        this.itemInput = itemInput;
        this.fluidInput = fluidInput;
        this.fluidOutput = fluidOutput;
        this.id = id;
    }

    @Override
    public boolean matches(CrucibleStorage inventory)
    {
        return itemInput.testStorage(inventory.getItemStorage(null))
                && fluidInput.testStorage(inventory.getStorage(null));
    }

    @Override
    public boolean takeInputs(CrucibleStorage context, TransactionContext transaction)
    {
        return false;
    }

    @Override
    public boolean ejectOutputs(CrucibleStorage context, TransactionContext transaction)
    {
        return false;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return false;
    }

    public RegistryRecipeInput<Item> getItemInput()
    {
        return itemInput;
    }

    public RegistryRecipeInput<Fluid> getFluidInput()
    {
        return fluidInput;
    }

    public RecipeOutput<Fluid> getFluidOutput()
    {
        return fluidOutput;
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    public MeatRecipeSerialiser<?> getSerializer()
    {
        return NMrecipeTypes.RENDERING_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType()
    {
        return NMrecipeTypes.RENDERING;
    }

    public ItemVariant takeInputs(CrucibleStorage storage, int amount, TransactionContext transaction)
    {
        ItemVariant item = null;

        try (Transaction take = transaction.openNested())
        {
            Storage<ItemVariant> itemStorage = storage.getItemStorage(null);
            Storage<FluidVariant> fluidStorage = storage.getStorage(null);

            item = itemInput.getFirstMatching(itemStorage, ItemVariant::of).orElse(null);
            Optional<FluidVariant> fluid = fluidInput.getFirstMatching(fluidStorage, FluidVariant::of);

            if (item == null || fluid.isEmpty())
            {
                return null;
            }

            long ex1 = itemStorage.extract(item, itemInput.amount() * amount, take);
            long ex2 = fluidStorage.extract(fluid.get(), fluidInput.amount() * amount, take);
            if (ex1 == itemInput.amount() * amount && ex2 == fluidInput.amount() * amount)
            {
                take.commit();
            }
            else
            {
                take.abort();
                return null;
            }
        }

        try (Transaction eject = transaction.openNested())
        {
            boolean bl1 = true;
            for (int i = 0; i < amount; ++i)
            {
                bl1 = bl1 && fluidOutput.insertInto(storage.getFluidOutput(), FluidVariant::of, eject);
            }

            if (bl1)
            {
                eject.commit();
                return item;
            }
            else
            {
                eject.abort();
            }
        }

        return null;
    }

    public static class Serializer implements MeatRecipeSerialiser<RenderingRecipe>
    {
        private final RecipeFactory<RenderingRecipe> factory;
        private final Codec<RecipeFunction<RenderingRecipe>> codec;

        public Serializer(RecipeFactory<RenderingRecipe> recipeFactory)
        {
            this.factory = recipeFactory;

                codec = RecordCodecBuilder.create(instance -> instance.group(
                        RecipeInputs.ITEM.codec().fieldOf("item_input").forGetter(MeatLibCodecs::notImplemented),
                        RecipeInputs.FLUID.codec().fieldOf("fluid_input").forGetter(MeatLibCodecs::notImplemented),
                        RecipeOutputs.FLUID.fieldOf("output").forGetter(MeatLibCodecs::notImplemented))
                    .apply(instance, (ii, fi, o) -> id -> new RenderingRecipe(id, ii, fi, o))
                );
        }

        @Override
        public Codec<RecipeFunction<RenderingRecipe>> codec()
        {
            return codec;
        }

        @Override
        public RenderingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
        {
            RegistryRecipeInput<Item> itemInput = RegistryRecipeInput.fromBuffer(buf);
            RegistryRecipeInput<Fluid> fluidInput = RegistryRecipeInput.fromBuffer(buf);
            RecipeOutputImpl<Fluid> fluidOutput = RecipeOutputImpl.read(BuiltInRegistries.FLUID, buf);

            return this.factory.create(id, itemInput, fluidInput, fluidOutput);
        }

        @Override
        public void write(FriendlyByteBuf buf, RenderingRecipe recipe)
        {
            recipe.itemInput.write(buf);
            recipe.fluidInput.write(buf);
            recipe.fluidOutput.write(buf);
        }

        @FunctionalInterface
        public interface RecipeFactory<T extends RenderingRecipe>
        {
            T create(ResourceLocation var1, RegistryRecipeInput<Item> in, RegistryRecipeInput<Fluid> in2, RecipeOutput<Fluid> out);
        }
    }
}
