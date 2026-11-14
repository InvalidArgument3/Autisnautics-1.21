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
import com.neep.meatlib.recipe.ingredient.RecipeOutput;
import com.neep.meatlib.recipe.ingredient.RecipeOutputImpl;
import com.neep.meatlib.recipe.ingredient.RecipeOutputs;
import com.neep.meatlib.util.MeatLibCodecs;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public class VivisectionRecipe implements MeatlibRecipe<VivisectionRecipe.VivisectionContext>
{
    private final ResourceLocation id;
    private final EntityType<?> entityType;
    private final int maxHealth;
    private final List<RecipeOutput<Item>> output;

    public VivisectionRecipe(ResourceLocation id, EntityType<?> entityType, int maxHealth, List<RecipeOutput<Item>> output)
    {
        this.id = id;
        this.entityType = entityType;
        this.maxHealth = maxHealth;
        this.output = output;
    }

    @Override
    public boolean matches(VivisectionContext context)
    {
        return context.entity.getType().equals(entityType) && context.entity.getHealth() <= maxHealth;
    }

    @Override
    public boolean takeInputs(VivisectionContext context, TransactionContext transaction)
    {
        return true;
    }

    @Override
    public boolean ejectOutputs(VivisectionContext context, TransactionContext transaction)
    {
        for (var recipeOutput : output)
        {
            Item resource = recipeOutput.resource();
            int amount = (int) recipeOutput.randomAmount(0);
            ItemEntity item = new ItemEntity(context.world, context.pos.x, context.pos.y, context.pos.z, new ItemStack(resource, amount));
            context.world.addFreshEntity(item);
            context.entity.kill();
        }
        return true;
    }

    @Override
    public MeatRecipeSerialiser<?> getSerializer()
    {
        return NMrecipeTypes.VIVISECTION_SERIALISER;
    }

    @Override
    public RecipeType<?> getType()
    {
        return NMrecipeTypes.VIVISECTION;
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    public EntityType<?> getEntityType()
    {
        return entityType;
    }

    public List<RecipeOutput<Item>> getRecipeOutput()
    {
        return output;
    }

    public static class Serializer implements MeatRecipeSerialiser<VivisectionRecipe>
    {
        private final Codec<RecipeFunction<VivisectionRecipe>> codec = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(MeatLibCodecs::notImplemented),
                Codec.INT.fieldOf("max_health").forGetter(MeatLibCodecs::notImplemented),
                Codec.list(RecipeOutputs.ITEM).fieldOf("output").forGetter(MeatLibCodecs::notImplemented)
        ).apply(instance, (e, h, o) -> id -> new VivisectionRecipe(id, e, h, o)));

        @Override
        public Codec<RecipeFunction<VivisectionRecipe>> codec()
        {
            return codec;
        }

        @Override
        public VivisectionRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
        {
            EntityType<?> type = buf.readById(BuiltInRegistries.ENTITY_TYPE::byId);
            int maxHealth = buf.readVarInt();
            List<RecipeOutput<Item>> output = RecipeOutputImpl.readList(BuiltInRegistries.ITEM, buf);
            return new VivisectionRecipe(id, type, maxHealth, output);
        }

        @Override
        public void write(FriendlyByteBuf buf, VivisectionRecipe recipe)
        {
            buf.writeById(BuiltInRegistries.ENTITY_TYPE::getId, recipe.entityType);
            buf.writeVarInt(recipe.maxHealth);
            RecipeOutputImpl.writeList(BuiltInRegistries.ITEM, recipe.output, buf);
        }
    }

    public static class VivisectionContext
    {
        private final Level world;
        private final LivingEntity entity;
        private final Vec3 pos;

        public VivisectionContext(Level world, LivingEntity entity)
        {
            this.world = world;
            this.entity = entity;
            pos = entity.position().add(0, 0.5, 0);
        }
    }
}
