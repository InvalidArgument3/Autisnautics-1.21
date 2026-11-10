/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.recipe;

import com.neep.neepmeat.NeepMeat;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeepRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, NeepMeat.ID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, NeepMeat.ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<AlloyKilnRecipe>> ALLOY = TYPES.register("alloy_smelting",
            () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "alloy_smelting")));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AlloyKilnRecipe>> ALLOY_SERIALIZER =
            SERIALIZERS.register("alloy_smelting", () -> AlloyKilnRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeType<?>, RecipeType<ManufactureRecipe>> MANUFACTURE = TYPES.register("manufacture",
            () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "manufacture")));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ManufactureRecipe>> MANUFACTURE_SERIALIZER =
            SERIALIZERS.register("manufacture", () -> ManufactureRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeType<?>, RecipeType<EnlighteningRecipe>> ENLIGHTENING = TYPES.register("enlightening",
            () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "enlightening")));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EnlighteningRecipe>> ENLIGHTENING_SERIALIZER =
            SERIALIZERS.register("enlightening", () -> EnlighteningRecipe.SERIALIZER);

    private NeepRecipes() {}
}
