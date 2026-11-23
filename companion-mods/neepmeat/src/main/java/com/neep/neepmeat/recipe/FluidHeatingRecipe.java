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
import com.neep.neepmeat.machine.multitank.MultiTankBlockEntity;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class FluidHeatingRecipe implements MeatlibRecipe<MultiTankBlockEntity>
{
    protected ResourceLocation id;
    protected RegistryRecipeInput<Fluid> fluidInput;
    protected RecipeOutput<Fluid> fluidOutput;
    protected int processTime;

    public FluidHeatingRecipe(ResourceLocation id, RegistryRecipeInput<Fluid> fluidInput, RecipeOutput<Fluid> fluidOutput, int processTime)
    {
        this.fluidInput = fluidInput;
        this.fluidOutput = fluidOutput;
        this.processTime = processTime;
        this.id = id;
    }

    @Override
    public boolean matches(MultiTankBlockEntity inventory)
    {
        for (Map.Entry<FluidVariant, Long> entry : inventory.getStorage().getSlots())
        {
            if (fluidInput.test(entry.getKey(), entry.getValue()))
            {
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
        return NMrecipeTypes.HEATING_SERIALISER;
    }

    @Override
    public MeatRecipeType<?> getType()
    {
        return NMrecipeTypes.HEATING;
    }

    public int getTime()
    {
        return processTime;
    }

    @Override
    public boolean takeInputs(MultiTankBlockEntity storage, TransactionContext transaction)
    {
        try (Transaction inner = transaction.openNested())
        {
            Optional<FluidVariant> fluid = fluidInput.getFirstMatching(storage.getStorage(), FluidVariant::of);
            if (!fluid.isPresent())
            {
                return false;
            }

            long extracted = storage.getStorage().extract(fluid.get(), fluidInput.amount(), transaction);
            if (extracted == fluidInput.amount())
            {
                inner.commit();
                return true;
            }
            inner.abort();
        }
        return false;
    }

    @Override
    public boolean ejectOutputs(MultiTankBlockEntity context, TransactionContext transaction)
    {
        try (Transaction inner = transaction.openNested())
        {
            boolean bl1 = fluidOutput.insertInto(context.getStorage(), FluidVariant::of, inner);

            if (bl1)
            {
                inner.commit();
                return true;
            }
            inner.abort();
        }
        return false;
    }

    public static class Serializer implements MeatRecipeSerialiser<FluidHeatingRecipe>
    {
        private final Codec<RecipeFunction<FluidHeatingRecipe>> codec;

        public Serializer(int processTime)
        {
            this.codec = RecordCodecBuilder.create(instance -> instance.group(
                    MeatLibCodecs.decodeOnly(RecipeInputs.FLUID.codec().fieldOf("input")),
                    MeatLibCodecs.decodeOnly(RecipeOutputs.FLUID.fieldOf("output")),
                    MeatLibCodecs.decodeOnly(Codec.INT.optionalFieldOf("processtime", processTime))
            ).apply(instance, (i, o, t) -> id -> new FluidHeatingRecipe(id, i, o, t)));
        }

        @Override
        public Codec<RecipeFunction<FluidHeatingRecipe>> codec()
        {
            return codec;
        }

        @Override
        public FluidHeatingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
        {
            RegistryRecipeInput<Fluid> fluidInput = RegistryRecipeInput.fromBuffer(buf);
            RecipeOutput<Fluid> fluidOutput = RecipeOutputImpl.read(BuiltInRegistries.FLUID, buf);

            int time = buf.readVarInt();

            return new FluidHeatingRecipe(id, fluidInput, fluidOutput, time);
        }

        @Override
        public void write(FriendlyByteBuf buf, FluidHeatingRecipe recipe)
        {
            recipe.fluidInput.write(buf);
            recipe.fluidOutput.write(buf);

            buf.writeVarInt(recipe.processTime);
        }
    }
}
