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

import com.neep.neepmeat.machine.small_trommel.TrommelStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Upstream read the recipe manager through an accessor. This binds the same manager
 * when the server finishes starting, which is before game tests run.
 */
public final class MeatlibRecipes {
    private static final MeatlibRecipes INSTANCE = new MeatlibRecipes();
    private @Nullable RecipeManager manager;

    private MeatlibRecipes() {}

    public static MeatlibRecipes getInstance() {
        return INSTANCE;
    }

    public static void onServerStarted(ServerStartedEvent event) {
        INSTANCE.manager = event.getServer().getRecipeManager();
    }

    public Optional<com.neep.neepmeat.recipe.CrushingRecipe> matchCrushing(RecipeType<com.neep.neepmeat.recipe.CrushingRecipe> type, ItemStack stack) {
        if (manager == null) {
            return Optional.empty();
        }
        for (RecipeHolder<com.neep.neepmeat.recipe.CrushingRecipe> holder : manager.getAllRecipesFor(type)) {
            if (holder.value().accepts(stack)) {
                return Optional.of(holder.value());
            }
        }
        return Optional.empty();
    }

    public Optional<com.neep.neepmeat.recipe.NormalTrommelRecipe> matchTrommel(RecipeType<com.neep.neepmeat.recipe.NormalTrommelRecipe> type, TrommelStorage storage) {
        if (manager == null) {
            return Optional.empty();
        }
        for (RecipeHolder<com.neep.neepmeat.recipe.NormalTrommelRecipe> holder : manager.getAllRecipesFor(type)) {
            if (holder.value().matches(storage)) {
                return Optional.of(holder.value());
            }
        }
        return Optional.empty();
    }

    public Optional<com.neep.neepmeat.recipe.BallMillRecipe> matchBallMill(RecipeType<com.neep.neepmeat.recipe.BallMillRecipe> type, ItemStack stack) {
        if (manager == null) {
            return Optional.empty();
        }
        for (RecipeHolder<com.neep.neepmeat.recipe.BallMillRecipe> holder : manager.getAllRecipesFor(type)) {
            if (holder.value().accepts(stack)) {
                return Optional.of(holder.value());
            }
        }
        return Optional.empty();
    }

    public Optional<com.neep.neepmeat.recipe.BioreactorRecipe> matchBioreactor(RecipeType<com.neep.neepmeat.recipe.BioreactorRecipe> type, com.neep.neepmeat.machine.live_machine.process.BioreactorProcess.Context context) {
        if (manager == null) {
            return Optional.empty();
        }
        for (RecipeHolder<com.neep.neepmeat.recipe.BioreactorRecipe> holder : manager.getAllRecipesFor(type)) {
            if (holder.value().accepts(context)) {
                return Optional.of(holder.value());
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Optional<RecipeHolder<?>> getFirstHolder(RecipeType<?> type, Object input) {
        if (manager == null || type == null) {
            return Optional.empty();
        }
        for (Object holderObj : manager.getAllRecipesFor((RecipeType) type)) {
            RecipeHolder<?> holder = (RecipeHolder<?>) holderObj;
            if (holder.value() instanceof RecipeContextMatch match && match.matches(input, null)) {
                return Optional.of(holder);
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> Optional<T> getFirstMatch(RecipeType<?> type, Object input) {
        return getFirstHolder(type, input).map(holder -> (T) holder.value());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> java.util.List<T> getAllValuesOfType(RecipeType<?> type) {
        if (manager == null || type == null) {
            return java.util.List.of();
        }
        java.util.List<T> values = new java.util.ArrayList<>();
        for (Object holderObj : manager.getAllRecipesFor((RecipeType) type)) {
            RecipeHolder<?> holder = (RecipeHolder<?>) holderObj;
            values.add((T) holder.value());
        }
        return values;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public java.util.stream.Stream<RecipeHolder<?>> holdersOfType(RecipeType<?> type) {
        if (manager == null || type == null) {
            return java.util.stream.Stream.empty();
        }
        java.util.List<RecipeHolder<?>> holders = new java.util.ArrayList<>();
        for (Object holderObj : manager.getAllRecipesFor((RecipeType) type)) {
            holders.add((RecipeHolder<?>) holderObj);
        }
        return holders.stream();
    }

    public Optional<?> get(ResourceLocation id) {
        if (manager == null || id == null) {
            return Optional.empty();
        }
        return manager.byKey(id).map(RecipeHolder::value);
    }
}
