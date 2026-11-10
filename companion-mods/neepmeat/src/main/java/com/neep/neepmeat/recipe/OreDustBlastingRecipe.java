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

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neep.meatlib.recipe.MeatRecipeSerialiser;
import com.neep.meatlib.util.MeatLibCodecs;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.item.AbstractOreDustItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

// Someone might assume that all recipes in BLASTING will inherit from BlastingRecipe.
// So I have to extend BlastingRecipe instead of OreDustSmeltingRecipe.
public class OreDustBlastingRecipe extends BlastingRecipe implements GeneralOreDustSmeltingRecipe
{
    private final Item inputDustType;

    public OreDustBlastingRecipe(ResourceLocation id, Item inputDustType, float experience, int cookTime)
    {
        super(id, "", CookingBookCategory.MISC, Ingredient.EMPTY, ItemStack.EMPTY, experience, cookTime);
        this.inputDustType = inputDustType;
    }

    public boolean matches(Container inventory, Level world)
    {
        ItemStack stack = inventory.getItem(0);
        if (stack.getItem() instanceof AbstractOreDustItem oreDust)
        {
            ItemStack result = oreDust.getOutput(stack);
            return !result.isEmpty();
        }

        return false;
    }

    @Override
    public ItemStack getOutput(RegistryAccess registryManager, ItemStack input)
    {
        if (input.getItem() instanceof AbstractOreDustItem oreDust)
        {
            return oreDust.getOutput(input);
        }

        return ItemStack.EMPTY;
    }

    public ItemStack assemble(Container inventory, RegistryAccess registryManager)
    {
        return getOutput(registryManager, inventory.getItem(0)).copy();
    }

    @Override
    public RecipeType<?> getType()
    {
        return RecipeType.BLASTING;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return NMrecipeTypes.ORE_DUST_BLASTING_SERIALISER;
    }

    public Item getInputDustType()
    {
        return inputDustType;
    }

    public static class Serialisier<T extends OreDustBlastingRecipe> implements RecipeSerializer<T>
    {
        private final Factory<T> factory;
        private final int cookTime;

        private final Codec<MeatRecipeSerialiser.RecipeFunction<T>> codec;

        public Serialisier(Factory<T> factory, int cookTime)
        {
            this.factory = factory;
            this.cookTime = cookTime;

            codec = RecordCodecBuilder.create(instance -> instance.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("dust_item").forGetter(MeatLibCodecs::notImplemented),
                    Codec.FLOAT.optionalFieldOf("experience", 0.7f).forGetter(MeatLibCodecs::notImplemented)
                ).apply(instance, (dustItem, xp) -> id -> factory.apply(id, dustItem, xp, this.cookTime)));
        }

        public T fromJson(ResourceLocation id, JsonObject json)
        {
            return codec.parse(JsonOps.INSTANCE, json).getOrThrow(true, s ->
            {
                throw new JsonParseException(s);
            }).apply(id);
        }

        public void write(FriendlyByteBuf buf, OreDustBlastingRecipe recipe)
        {
            buf.writeById(BuiltInRegistries.ITEM::getId, recipe.inputDustType);
            buf.writeFloat(recipe.experience);
        }

        public T fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
        {
            Item dust = buf.readById(BuiltInRegistries.ITEM::byId);
            float xp = buf.readFloat();

            return factory.apply(id, dust, xp, cookTime);
        }
    }

    public interface Factory<T extends OreDustBlastingRecipe>
    {
        T apply(ResourceLocation id, Item dust, float xp, int cookTime);
    }
}
