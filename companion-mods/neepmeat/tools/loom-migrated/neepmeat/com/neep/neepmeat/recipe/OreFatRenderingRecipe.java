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
import com.neep.neepmeat.api.processing.OreFatRegistry;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.crucible.CrucibleStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class OreFatRenderingRecipe implements RenderingBasinRecipe
{
    protected ResourceLocation id;
    protected RegistryRecipeInput<Fluid> fluidInput;
    protected RecipeOutput<Fluid> fluidOutput;

    public OreFatRenderingRecipe(ResourceLocation id, RegistryRecipeInput<Fluid> fluidInput, RecipeOutput<Fluid> fluidOutput)
    {
        this.fluidInput = fluidInput;
        this.fluidOutput = fluidOutput;
        this.id = id;
    }

    @Override
    public boolean matches(CrucibleStorage inventory)
    {
        boolean rightFluid = false;
        for (StorageView<? extends TransferVariant<?>> view : inventory.getStorage(null))
        {
            if (fluidInput.test(view.getResource(), (long) (view.getAmount() * (1 + inventory.discount()))))
                rightFluid = true;
        }

        if (rightFluid)
        {
            for (var view : inventory.getItemStorage(null).nonEmptyViews())
            {
                if (OreFatRegistry.getFromRecipeInput(view.getResource()) != null)
                    return true;
            }
        }
        return false;
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
        return NMrecipeTypes.ORE_FAT_RENDERING_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType()
    {
        return NMrecipeTypes.ORE_FAT_RENDERING;
    }

    public ItemVariant takeInputs(CrucibleStorage storage, int itemAmount, TransactionContext transaction)
    {
        Storage<ItemVariant> itemStorage = storage.getItemStorage(null);
        Storage<FluidVariant> fluidStorage = storage.getStorage(null);

        ItemVariant item = null;
        OreFatRegistry.Entry entry = null;
        for (var view : itemStorage.nonEmptyViews())
        {
            entry = OreFatRegistry.getFromRecipeInput(view.getResource());
            if (entry != null)
            {
                item = view.getResource();
                break;
            }
        }

        if (item == null && entry != null)
            return null;

        try (Transaction take = transaction.openNested())
        {
            Optional<FluidVariant> fluid = fluidInput.getFirstMatching(fluidStorage, FluidVariant::of);
            if (fluid.isEmpty())
                return null;

            long fluidToExtract = (long) (fluidInput.amount() * itemAmount * (1 - storage.discount()));
            long ex1 = itemStorage.extract(item, itemAmount, take);
            long ex2 = fluidStorage.extract(fluid.get(), fluidToExtract, take);
            if (ex1 == itemAmount && ex2 == fluidToExtract)
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
            FluidVariant outputVariant = FluidVariant.of(fluidOutput.resource(), entry.nbt());
            for (int i = 0; i < itemAmount; ++i)
            {
                bl1 = bl1 && storage.getFluidOutput().insert(outputVariant,
                        FluidConstants.INGOT * fluidOutput.randomAmount(entry.renderingYield() - 1),
                        eject) > 0;
            }

            if (bl1)
            {
                eject.commit();
                return item;
            }
            else eject.abort();
        }
        return null;
    }

    public static class Serializer implements MeatRecipeSerialiser<OreFatRenderingRecipe>
    {
        RecipeFactory<OreFatRenderingRecipe> factory;

        private final Codec<RecipeFunction<OreFatRenderingRecipe>> codec;

        public Serializer(RecipeFactory<OreFatRenderingRecipe> recipeFactory)
        {
            this.factory = recipeFactory;

            codec = RecordCodecBuilder.create(instance -> instance.group(
                    RecipeInputs.FLUID.codec().fieldOf("fluid_input").forGetter(MeatLibCodecs::notImplemented),
                    RecipeOutputs.FLUID.fieldOf("output").forGetter(MeatLibCodecs::notImplemented))
                    .apply(instance, (i, o) -> id -> factory.create(id, i, o)));
        }

        @Override
        public Codec<RecipeFunction<OreFatRenderingRecipe>> codec()
        {
            return codec;
        }

//        @Override
//        public OreFatRenderingRecipe read(Identifier id, JsonObject json)
//        {
//            JsonObject fluidInputElement = JsonHelper.getObject(json, "fluid_input");
//            RegistryRecipeInput<Fluid> fluidInput = RegistryRecipeInput.fromJson(RecipeInputs.FLUID, fluidInputElement);
//
//            JsonObject outputElement = JsonHelper.getObject(json, "output");
//            RecipeOutput<Fluid> fluidOutput = RecipeOutputImpl.fromJson(Ingredients.FLUID, outputElement);
//
//            return this.factory.create(id, fluidInput, fluidOutput);
//        }

        @Override
        public OreFatRenderingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
        {
            RegistryRecipeInput<Fluid> fluidInput = RegistryRecipeInput.fromBuffer(buf);
            RecipeOutputImpl<Fluid> fluidOutput = RecipeOutputImpl.read(BuiltInRegistries.FLUID, buf);

            return this.factory.create(id, fluidInput, fluidOutput);
        }

        @Override
        public void write(FriendlyByteBuf buf, OreFatRenderingRecipe recipe)
        {
            recipe.fluidInput.write(buf);
            recipe.fluidOutput.write(buf);
        }

        @FunctionalInterface
        public interface RecipeFactory<T extends OreFatRenderingRecipe>
        {
            T create(ResourceLocation var1, RegistryRecipeInput<Fluid> in2, RecipeOutput<Fluid> out);
        }
    }
}
