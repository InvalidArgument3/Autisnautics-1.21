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

    public static final DeferredHolder<RecipeType<?>, RecipeType<CrushingRecipe>> GRINDING = TYPES.register("grinding",
            () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "grinding")));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CrushingRecipe>> GRINDING_SERIALIZER =
            SERIALIZERS.register("grinding", () -> crushingSerializer(GRINDING, 60));

    public static final DeferredHolder<RecipeType<?>, RecipeType<CrushingRecipe>> ADVANCED_CRUSHING = TYPES.register("advanced_crushing",
            () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "advanced_crushing")));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CrushingRecipe>> ADVANCED_CRUSHING_SERIALIZER =
            SERIALIZERS.register("advanced_crushing", () -> crushingSerializer(ADVANCED_CRUSHING, 60));

    public static final DeferredHolder<RecipeType<?>, RecipeType<com.neep.neepmeat.recipe.NormalTrommelRecipe>> TROMMEL = TYPES.register("trommel",
            () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "trommel")));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<com.neep.neepmeat.recipe.NormalTrommelRecipe>> TROMMEL_SERIALIZER =
            SERIALIZERS.register("trommel", () -> trommelSerializer(TROMMEL));

    public static final DeferredHolder<RecipeType<?>, RecipeType<com.neep.neepmeat.recipe.NormalTrommelRecipe>> FAT_TROMMEL = TYPES.register("fat_trommel",
            () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "fat_trommel")));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<com.neep.neepmeat.recipe.NormalTrommelRecipe>> FAT_TROMMEL_SERIALIZER =
            SERIALIZERS.register("fat_trommel", () -> new RecipeSerializer<>() {
                @Override
                public com.mojang.serialization.MapCodec<com.neep.neepmeat.recipe.NormalTrommelRecipe> codec() {
                    com.neep.meatlib.recipe.ResourceSpec none = new com.neep.meatlib.recipe.ResourceSpec(java.util.Optional.empty(), java.util.Optional.empty(), Long.MAX_VALUE, -1, -1, 1f, java.util.Optional.empty());
                    return com.mojang.serialization.MapCodec.unit(new com.neep.neepmeat.recipe.NormalTrommelRecipe(none, none, null, FAT_TROMMEL.get(), this));
                }

                @Override
                public net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, com.neep.neepmeat.recipe.NormalTrommelRecipe> streamCodec() {
                    return net.minecraft.network.codec.StreamCodec.unit(codec().codec().parse(com.mojang.serialization.JsonOps.INSTANCE, com.google.gson.JsonParser.parseString("{}")).getOrThrow());
                }
            });

    public static final DeferredHolder<RecipeType<?>, RecipeType<BioreactorRecipe>> BIOREACTOR = TYPES.register("bioreactor",
            () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "bioreactor")));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BioreactorRecipe>> BIOREACTOR_SERIALIZER =
            SERIALIZERS.register("bioreactor", () -> BioreactorRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeType<?>, RecipeType<BallMillRecipe>> BALL_MILL = TYPES.register("ball_mill",
            () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "ball_mill")));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BallMillRecipe>> BALL_MILL_SERIALIZER =
            SERIALIZERS.register("ball_mill", () -> ballMillSerializer(BALL_MILL));

    private NeepRecipes() {}

    private static RecipeSerializer<CrushingRecipe> crushingSerializer(DeferredHolder<RecipeType<?>, RecipeType<CrushingRecipe>> type, int time) {
        return new RecipeSerializer<>() {
            @Override
            public com.mojang.serialization.MapCodec<CrushingRecipe> codec() {
                return CrushingRecipe.codec(type.get(), this, time);
            }

            @Override
            public net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, CrushingRecipe> streamCodec() {
                return CrushingRecipe.streamCodec(type.get(), this);
            }
        };
    }

    private static RecipeSerializer<com.neep.neepmeat.recipe.NormalTrommelRecipe> trommelSerializer(DeferredHolder<RecipeType<?>, RecipeType<com.neep.neepmeat.recipe.NormalTrommelRecipe>> type) {
        return new RecipeSerializer<>() {
            @Override
            public com.mojang.serialization.MapCodec<com.neep.neepmeat.recipe.NormalTrommelRecipe> codec() {
                return com.neep.neepmeat.recipe.NormalTrommelRecipe.codec(type.get(), this);
            }

            @Override
            public net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, com.neep.neepmeat.recipe.NormalTrommelRecipe> streamCodec() {
                return com.neep.neepmeat.recipe.NormalTrommelRecipe.streamCodec(type.get(), this);
            }
        };
    }

    private static RecipeSerializer<BallMillRecipe> ballMillSerializer(DeferredHolder<RecipeType<?>, RecipeType<BallMillRecipe>> type) {
        return new RecipeSerializer<>() {
            @Override
            public com.mojang.serialization.MapCodec<BallMillRecipe> codec() {
                return BallMillRecipe.codec(type.get(), this);
            }

            @Override
            public net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, BallMillRecipe> streamCodec() {
                return BallMillRecipe.streamCodec(type.get(), this);
            }
        };
    }
}
