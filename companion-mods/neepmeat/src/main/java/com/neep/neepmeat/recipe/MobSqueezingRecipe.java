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
import com.neep.meatlib.recipe.ingredient.*;
import com.neep.meatlib.recipe.ingredient.entity.EntityRecipeInput;
import com.neep.meatlib.util.MeatLibCodecs;
import com.neep.neepmeat.init.NMParticles;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.hydraulic_press.MobSqueezeContext;
import com.neep.neepmeat.mixin.client.LivingEntityAccessor;
import com.neep.neepmeat.transport.block.fluid_transport.entity.FluidDrainBlockEntity;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

@SuppressWarnings("UnstableApiUsage")
public class MobSqueezingRecipe implements MeatlibRecipe<MobSqueezeContext>
{
    protected final ResourceLocation id;
    protected final EntityRecipeInput entityInput;
    protected final RecipeOutput<?> output;

    public MobSqueezingRecipe(ResourceLocation id, EntityRecipeInput entityInput, RecipeOutput<?> output)
    {
        this.entityInput = entityInput;
        this.output = output;
        this.id = id;
    }

    @Override
    public boolean matches(MobSqueezeContext context)
    {
        return context.entity().stream().anyMatch(entityInput);
    }

    @Override
    public boolean takeInputs(MobSqueezeContext context, TransactionContext transaction) { return true; }

    @Override
    public boolean ejectOutputs(MobSqueezeContext context, TransactionContext transaction)
    {
        if (context.entity().isEmpty())
            return false;

        Level world = context.world();

        // jank
        if (output.getType() == Ingredients.FLUID)
        {
            if (world.getBlockEntity(context.pos().below(2)) instanceof FluidDrainBlockEntity be)
            {
                ((RecipeOutput<Fluid>) output).insertInto(be.getBuffer(null), FluidVariant::of, transaction);
            }
        }
        else if (output.getType() == Ingredients.ITEM)
        {
            BlockPos pos = context.pos();
            ItemStack stack = new ItemStack((ItemLike) output.resource(), (int) output.randomAmount(0));
            ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, stack);
            world.addFreshEntity(entity);
        }

        Entity entity = context.entity().get(0);

        if (entity instanceof LivingEntity livingEntity)
        {
            world.playSound(null, context.pos().getX(), context.pos().getY(), context.pos().getZ(),
                    ((LivingEntityAccessor) livingEntity).getHurtSound(world.damageSources().generic()),
                    SoundSource.AMBIENT,
                    1, 1);
        }

        if (world instanceof ServerLevel serverWorld)
        {
            serverWorld.sendParticles(NMParticles.MILK_SPLASH,
                    context.pos().getX() + 0.5, context.pos().getY() + 0.5, context.pos().getZ() + 0.5,
                    10, 0.25, 0.25, 0.25, 0.0001);
        }

        return true;
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    public MeatRecipeSerialiser<?> getSerializer()
    {
        return NMrecipeTypes.MOB_SQUEEZING_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType()
    {
        return NMrecipeTypes.MOB_SQUEEZING;
    }

    public static class Serializer implements MeatRecipeSerialiser<MobSqueezingRecipe>
    {
        private final Codec<RecipeFunction<MobSqueezingRecipe>> codec = RecordCodecBuilder.create(instance -> instance.group(
                RecipeInputs.ENTITY.codec().fieldOf("entity").forGetter(MeatLibCodecs::notImplemented),
                RecipeOutputs.ANY.fieldOf("output").forGetter(MeatLibCodecs::notImplemented)
        ).apply(instance, (i, o) -> id -> new MobSqueezingRecipe(id, i, o)));

        public Serializer()
        {
        }

        @Override
        public Codec<RecipeFunction<MobSqueezingRecipe>> codec()
        {
            return codec;
        }

        @Override
        public MobSqueezingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
        {
            var entityType = RecipeInputs.ENTITY.fromBuffer(buf);
            var itemOutput = RecipeOutputImpl.read(buf);

            return new MobSqueezingRecipe(id, entityType, itemOutput);
        }

        @Override
        public void write(FriendlyByteBuf buf, MobSqueezingRecipe recipe)
        {
            RecipeInputs.ENTITY.write(buf, recipe.entityInput);
            recipe.output.write(buf);
        }
    }
}
