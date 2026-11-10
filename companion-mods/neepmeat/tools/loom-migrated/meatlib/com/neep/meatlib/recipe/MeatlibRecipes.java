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

import com.neep.meatlib.mixin.RecipeManagerAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A custom interface for RecipeManager.
 * Advantages:
 * - Block entities can load their recipes without access to a World instance.
 * - MeatlibRecipe does not have its generic bounded by Inventory.
 * - No need to override and implement the irrelevant, useless methods in Recipe
 * - That's about it, I suppose.
 * - It's slightly cursed.
 */
public interface MeatlibRecipes
{
    static void init()
    {
        ServerLifecycleEvents.SERVER_STARTING.register(server ->
        {
            MeatlibRecipesImpl.INSTANCE = new MeatlibRecipesImpl(server::getRecipeManager);
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server ->
        {
            MeatlibRecipesImpl.INSTANCE = EMPTY;
        });
    }

    @Environment(EnvType.CLIENT)
    static void initClient()
    {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
        {
            if (!client.hasSingleplayerServer())
            {
                MeatlibRecipesImpl.INSTANCE = new MeatlibRecipesImpl(() -> client.getConnection().getRecipeManager());
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
        {
            if (!client.isLocalServer())
            {
                MeatlibRecipesImpl.INSTANCE = EMPTY;
            }
        });
    }

    static MeatlibRecipes getInstance()
    {
        return MeatlibRecipesImpl.INSTANCE;
    }

    Optional<? extends MeatlibRecipe<?>> get(ResourceLocation id);

    <C, T extends MeatlibRecipe<C>> Optional<T> get(RecipeType<T> type, ResourceLocation id);

    <C extends Container, T extends Recipe<C>> Optional<T> getVanilla(RecipeType<T> type, ResourceLocation id);

    <C, T extends MeatlibRecipe<C>> Optional<T> getFirstMatch(MeatRecipeType<T> type, C context);

    <C, T extends MeatlibRecipe<C>> Map<ResourceLocation, T> getAllOfTypeSafe(MeatRecipeType<T> type);

    <C, T extends MeatlibRecipe<C>> Stream<T> getAllValuesOfType(MeatRecipeType<T> type);

    MeatlibRecipes EMPTY = new MeatlibRecipes()
    {
        @Override
        public Optional<? extends MeatlibRecipe<?>> get(ResourceLocation id)
        {
            return Optional.empty();
        }

        @Override
        public <C, T extends MeatlibRecipe<C>> Optional<T> get(RecipeType<T> type, ResourceLocation id)
        {
            return Optional.empty();
        }

        @Override
        public <C extends Container, T extends Recipe<C>> Optional<T> getVanilla(RecipeType<T> type, ResourceLocation id)
        {
            return Optional.empty();
        }

        @Override
        public <C, T extends MeatlibRecipe<C>> Optional<T> getFirstMatch(MeatRecipeType<T> type, C context)
        {
            return Optional.empty();
        }

        @Override
        public <C, T extends MeatlibRecipe<C>> Map<ResourceLocation, T> getAllOfTypeSafe(MeatRecipeType<T> type)
        {
            return Map.of();
        }

        @Override
        public <C, T extends MeatlibRecipe<C>> Stream<T> getAllValuesOfType(MeatRecipeType<T> type)
        {
            return Stream.empty();
        }
    };


    class MeatlibRecipesImpl implements MeatlibRecipes
    {

        public static MeatlibRecipes INSTANCE = EMPTY;
        private final Supplier<RecipeManager> recipeManager;

        private MeatlibRecipesImpl(Supplier<RecipeManager> recipeManagerSupplier)
        {
            this.recipeManager = recipeManagerSupplier;
        }

        @Override
        public Optional<? extends MeatlibRecipe<?>> get(ResourceLocation id)
        {
            var recipe = recipeManager.get().byKey(id);
            if (recipe.isPresent() && recipe.get() instanceof MeatlibRecipe<?> mlr)
            {
                return Optional.of(mlr);
            }

            return Optional.empty();
        }

        @Override
        public <C, T extends MeatlibRecipe<C>> Optional<T> get(RecipeType<T> type, ResourceLocation id)
        {
            var recipe = ((RecipeManagerAccessor) recipeManager.get()).getRecipesById().get(id);
            if (recipe == null || recipe.getType() != type)
                return Optional.empty();

            return Optional.ofNullable((T) recipe);
        }

        @Override
        public <C extends Container, T extends Recipe<C>> Optional<T> getVanilla(RecipeType<T> type, ResourceLocation id)
        {
            var recipe = ((RecipeManagerAccessor) recipeManager.get()).getRecipesById().get(id);
            if (recipe == null || recipe.getType() != type)
                return Optional.empty();

            return Optional.ofNullable((T) recipe);
        }

        @Override
        public <C, T extends MeatlibRecipe<C>> Optional<T> getFirstMatch(MeatRecipeType<T> type, C context)
        {
            return getAllValuesOfType(type)
                    .flatMap(recipe -> type.match(recipe, context).stream()).findFirst();
        }

        @Override
        public <C, T extends MeatlibRecipe<C>> Map<ResourceLocation, T> getAllOfTypeSafe(MeatRecipeType<T> type)
        {
            var map = ((RecipeManagerAccessor) recipeManager.get()).getRecipes().getOrDefault(type, Collections.emptyMap());

            return map.entrySet().stream()
                    .filter(e -> e.getValue() instanceof MeatlibRecipe<?>)
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> (T) e.getValue()));
        }

        @Override
        public <C, T extends MeatlibRecipe<C>> Stream<T> getAllValuesOfType(MeatRecipeType<T> type)
        {
            var map = ((RecipeManagerAccessor) recipeManager.get()).getRecipes().getOrDefault(type, Collections.emptyMap());

            return map.values().stream()
                    .filter(recipe -> recipe instanceof MeatlibRecipe<?>) // Just in case something wEiRd has been done by another mod
                    .map(recipe -> (T) recipe);
        }
    }
}
