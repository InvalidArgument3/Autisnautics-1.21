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
import com.neep.meatlib.recipe.ingredient.entity.EntityRecipeInput;
import com.neep.meatlib.util.MeatLibCodecs;
import com.neep.neepmeat.api.plc.recipe.ManufactureStep;
import com.neep.neepmeat.implant.entity.EntityImplantInstaller;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.plc.component.MutateInPlace;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import java.util.List;

public class EntityImplantRecipe extends EntityMutateRecipe
{
    private final EntityImplantInstaller implant;

    public EntityImplantRecipe(ResourceLocation id, EntityRecipeInput base, List<ManufactureStep<?>> steps, EntityImplantInstaller implant)
    {
        super(id, base, steps);
        this.implant = implant;
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
        implant.install(context.get());
        return false;
    }

    @Override
    public MeatRecipeType<?> getType()
    {
        return PLCRecipes.ENTITY_MANUFACTURE;
    }

    @Override
    public MeatRecipeSerialiser<?> getSerializer()
    {
        return PLCRecipes.ENTITY_MANUFACTURE_SERIALISER;
    }

    public static class Serialiser implements MeatRecipeSerialiser<EntityImplantRecipe>
    {
        private final Codec<RecipeFunction<EntityImplantRecipe>> codec = RecordCodecBuilder.create(instance -> instance.group(
                RecipeInputs.ENTITY.codec().fieldOf("base").forGetter(MeatLibCodecs::notImplemented),
                ManufactureStep.LIST_CODEC.fieldOf("steps").forGetter(MeatLibCodecs::notImplemented),
                EntityImplantInstaller.CODEC.fieldOf("implant_installer").forGetter(MeatLibCodecs::notImplemented)
        ).apply(instance, (b, s, r) -> id -> new EntityImplantRecipe(id, b, s, r)));

        @Override
        public Codec<RecipeFunction<EntityImplantRecipe>> codec()
        {
            return codec;
        }

        @Override
        public EntityImplantRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
        {
//            EntityType<?> base = buf.readRegistryValue(Registries.ENTITY_TYPE);
            EntityRecipeInput base = RecipeInputs.ENTITY.fromBuffer(buf);

            List<ManufactureStep<?>> steps = ItemManufactureRecipe.Serialiser.readSteps(buf);

            EntityImplantInstaller implant = buf.readById(EntityImplantInstaller.REGISTRY::byId);

            return new EntityImplantRecipe(id, base, steps, implant);
        }

        @Override
        public void write(FriendlyByteBuf buf, EntityImplantRecipe recipe)
        {
            recipe.base.write(buf);
//            buf.writeRegistryValue(Registries.ENTITY_TYPE, recipe.base);

            ItemManufactureRecipe.Serialiser.writeSteps(recipe.getSteps(), buf);

            buf.writeById(EntityImplantInstaller.REGISTRY::getId, recipe.implant);
        }
    }
}
