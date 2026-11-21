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

package com.neep.meatlib.recipe.ingredient.entity;

import com.mojang.serialization.Codec;
import com.neep.meatlib.recipe.ingredient.IngredientType;
import com.neep.meatlib.recipe.ingredient.Ingredients;
import com.neep.meatlib.recipe.ingredient.RecipeInput;
import com.neep.meatlib.recipe.ingredient.RecipeInputs;
import com.neep.meatlib.util.MeatLibCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public abstract class EntityRecipeInput implements RecipeInput<EntityType<?>, Entity>
{
    protected EntityRecipeInput()
    {

    }

    public Component format()
    {
        // I hate you
        if (this instanceof TypeEntityInput type)
        {
            return type.getEntityType().getDescription();
        }
        else if (this instanceof TypeFilterEntityInput filter)
        {
            return Component.nullToEmpty(filter.getName());
        }

        return Component.nullToEmpty("unknown");
    }

    @Override
    public EntityInputSerialiser getSerialiser()
    {
        return RecipeInputs.ENTITY;
    }

    @Override
    public IngredientType<?> getType()
    {
        return Ingredients.ENTITY_MUTATE;
    }

    @Override
    public void write(FriendlyByteBuf buf)
    {
        getSerialiser().write(buf, this);
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    public static class EntityInputSerialiser implements Serialiser<EntityType<?>, Entity, EntityRecipeInput>
    {
        private final Codec<EntityRecipeInput> codec = Codec.either(
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().xmap(TypeEntityInput::new, MeatLibCodecs::notImplemented).fieldOf("id").codec(),
                TypeFilterEntityInput.CODEC
        ).flatXmap(MeatLibCodecs.<EntityRecipeInput>collapseEither("expected 'id' or 'filter'"), MeatLibCodecs::notImplemented);

        public EntityInputSerialiser()
        {
        }

        @Override
        public EntityRecipeInput fromBuffer(FriendlyByteBuf buf)
        {
            // JAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAANK
            int id = buf.readInt();

            // the horror
            if (id == 0)
            {
                EntityType<?> type = buf.readById(BuiltInRegistries.ENTITY_TYPE::byId);
                return new TypeEntityInput(type);
            }
            else if (id == 1)
            {
                String name = buf.readUtf();
                return TypeFilterEntityInput.get(name);
            }

            return new TypeEntityInput(EntityType.PIG);
        }

        @Override
        public Codec<EntityRecipeInput> codec()
        {
            return codec;
        }

        @Override
        public void write(FriendlyByteBuf buf, EntityRecipeInput input)
        {
            // bad bad bad bad bad
            if (input instanceof TypeEntityInput typeEntityInput)
            {
                buf.writeInt(0);
                buf.writeById(BuiltInRegistries.ENTITY_TYPE::getId, typeEntityInput.getEntityType());
            }
            else if (input instanceof TypeFilterEntityInput filterEntityInput)
            {
                buf.writeInt(1);
                buf.writeUtf(filterEntityInput.getName());
            }
        }

        @Override
        public IngredientType<EntityType<?>> getType()
        {
            return Ingredients.ENTITY_MUTATE;
        }
    }
}
