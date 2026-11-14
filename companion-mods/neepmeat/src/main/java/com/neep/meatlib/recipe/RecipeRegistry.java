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

package com.neep.meatlib.recipe;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class RecipeRegistry
{
    public static <T extends Recipe<?>> RecipeSerializer<T> registerMeatlibSerializer(final String namespace, final String id, RecipeSerializer<T> serializer)
    {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, ResourceLocation.fromNamespaceAndPath(namespace, id), serializer);
    }

    public static <T extends Recipe<?>> RecipeType<T> registerType(final String namespace, final String id)
    {
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, ResourceLocation.fromNamespaceAndPath(namespace, id), new RecipeType<T>()
        {
            public String toString()
            {
                return id;
            }
        });
    }

    public static <T extends Recipe<?>> MeatRecipeType<T> registerMeatlibType(final String namespace, final String id)
    {
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, ResourceLocation.fromNamespaceAndPath(namespace, id), new MeatRecipeType<T>()
        {
            public String toString()
            {
                return id;
            }
        });
    }

    public static <T extends MeatlibRecipe<?>> MeatRecipeSerialiser<T> registerMeatlibSerializer(final String namespace, final String id, MeatRecipeSerialiser<T> serializer)
    {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, ResourceLocation.fromNamespaceAndPath(namespace, id), serializer);
    }
}
