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

package com.neep.neepmeat.plc.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neep.meatlib.recipe.MeatRecipeSerialiser;
import com.neep.meatlib.recipe.MeatRecipeType;
import com.neep.meatlib.recipe.ingredient.RecipeInputs;
import com.neep.meatlib.recipe.ingredient.RecipeOutput;
import com.neep.meatlib.recipe.ingredient.RecipeOutputImpl;
import com.neep.meatlib.recipe.ingredient.RecipeOutputs;
import com.neep.meatlib.recipe.ingredient.entity.EntityRecipeInput;
import com.neep.meatlib.util.MeatLibCodecs;
import com.neep.neepmeat.api.plc.recipe.ManufactureStep;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.plc.component.MutateInPlace;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public class EntityToItemRecipe extends EntityMutateRecipe
{
    private final RecipeOutput<Item> output;

    public EntityToItemRecipe(ResourceLocation id, EntityRecipeInput base, List<ManufactureStep<?>> steps, RecipeOutput<Item> output)
    {
        super(id, base, steps);
        this.output = output;
    }

    @Override
    public boolean matches(MutateInPlace<Entity> context)
    {
        Entity entity = context.get();

        if (!base.test(entity))
            return false;

        var workpiece = NMComponents.WORKPIECE.getNullable(entity);
        if (workpiece != null)
        {
            var workSteps = workpiece.getSteps();

            if (workSteps.size() < steps.size())
                return false;

            int difference = workSteps.size() - steps.size();

            int i = steps.size() - 1;
            while (i >= 0)
            {
                var workStep = workSteps.get(i + difference);
                var recipeStep = steps.get(i);

                if (!ManufactureStep.matches(recipeStep, workStep))
                {
                    return false;
                }

                i--;
            }
            return true;
        }
        return false;
    }

    @Override
    public EntityRecipeInput getBase()
    {
        return base;
    }

    public List<ManufactureStep<?>> getSteps()
    {
        return steps;
    }

    @Override
    public boolean takeInputs(MutateInPlace<Entity> context, TransactionContext transaction)
    {
        return false;
    }

    @Override
    public boolean ejectOutputs(MutateInPlace<Entity> context, TransactionContext transaction)
    {
        Entity entity = context.get();
        Vec3 pos = entity.position();

        Level world = entity.level();
        int amount = (int) output.randomAmount(0);
        ItemStack stack = new ItemStack(output.resource(), amount);
        stack.setTag(output.getNbt());
        world.addFreshEntity(new ItemEntity(world, pos.x, pos.y + 0.1, pos.z, stack, 0, 0.1, 0));
        entity.discard();

        return false;
    }

    @Override
    public MeatRecipeType<?> getType()
    {
        return PLCRecipes.ENTITY_TO_ITEM;
    }

    @Override
    public MeatRecipeSerialiser<?> getSerializer()
    {
        return PLCRecipes.ENTITY_TO_ITEM_SERIALISER;
    }

    public RecipeOutput<Item> getRecipeOutput()
    {
        return output;
    }

    public static class Serialiser implements MeatRecipeSerialiser<EntityToItemRecipe>
    {
        private final Codec<RecipeFunction<EntityToItemRecipe>> codec = RecordCodecBuilder.create(instance -> instance.group(
                RecipeInputs.ENTITY.codec().fieldOf("base").forGetter(MeatLibCodecs::notImplemented),
                ManufactureStep.LIST_CODEC.fieldOf("steps").forGetter(MeatLibCodecs::notImplemented),
                RecipeOutputs.ITEM.fieldOf("result").forGetter(MeatLibCodecs::notImplemented)
        ).apply(instance, (b, s, r) -> id -> new EntityToItemRecipe(id, b, s, r)));

        @Override
        public Codec<RecipeFunction<EntityToItemRecipe>> codec()
        {
            return codec;
        }

        @Override
        public EntityToItemRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
        {
//            EntityType<?> base = buf.readRegistryValue(Registries.ENTITY_TYPE);
            EntityRecipeInput base = RecipeInputs.ENTITY.fromBuffer(buf);

            List<ManufactureStep<?>> steps = ItemManufactureRecipe.Serialiser.readSteps(buf);

            RecipeOutput<Item> output = RecipeOutputImpl.read(BuiltInRegistries.ITEM, buf);

            return new EntityToItemRecipe(id, base, steps, output);
        }

        @Override
        public void write(FriendlyByteBuf buf, EntityToItemRecipe recipe)
        {
            recipe.base.write(buf);

            ItemManufactureRecipe.Serialiser.writeSteps(recipe.getSteps(), buf);

            recipe.output.write(buf);
        }
    }
}
