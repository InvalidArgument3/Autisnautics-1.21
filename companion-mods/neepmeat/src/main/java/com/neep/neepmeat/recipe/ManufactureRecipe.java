/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;

public record ManufactureRecipe(ResourceLocation base, List<ManufactureStepSpec> steps, ResourceLocation result, int amount)
        implements Recipe<ManufactureInput> {
    private static final MapCodec<IdRef> BASE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(IdRef::id)
    ).apply(instance, IdRef::new));
    private static final MapCodec<ResultRef> RESULT_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("resource").forGetter(ResultRef::resource),
            Codec.INT.optionalFieldOf("amount", 1).forGetter(ResultRef::amount)
    ).apply(instance, ResultRef::new));

    public static final MapCodec<ManufactureRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BASE_CODEC.fieldOf("base").forGetter(recipe -> new IdRef(recipe.base.toString())),
            ManufactureStepSpec.CODEC.listOf().fieldOf("steps").forGetter(ManufactureRecipe::steps),
            RESULT_CODEC.fieldOf("result").forGetter(recipe -> new ResultRef(recipe.result.toString(), recipe.amount))
    ).apply(instance, (base, steps, result) -> new ManufactureRecipe(
            ResourceLocation.parse(base.id()), steps, ResourceLocation.parse(result.resource()), result.amount())));

    public static final StreamCodec<RegistryFriendlyByteBuf, ManufactureRecipe> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ManufactureRecipe::base,
            ManufactureStepSpec.STREAM_CODEC.apply(ByteBufCodecs.list()), ManufactureRecipe::steps,
            ResourceLocation.STREAM_CODEC, ManufactureRecipe::result,
            ByteBufCodecs.INT, ManufactureRecipe::amount,
            ManufactureRecipe::new);

    public static final RecipeSerializer<ManufactureRecipe> SERIALIZER = new RecipeSerializer<>() {
        @Override
        public MapCodec<ManufactureRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ManufactureRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    };

    public static ItemStack apply(ItemStack stack, ManufactureStepSpec step, net.minecraft.world.item.crafting.RecipeManager recipes) {
        ItemStack copy = stack.copy();
        List<ManufactureStepSpec> next = new ArrayList<>(WorkpieceSteps.on(copy).steps());
        next.add(step);
        copy.set(NeepComponents.WORKPIECE.get(), new WorkpieceSteps(List.copyOf(next)));
        for (var holder : recipes.getAllRecipesFor(NeepRecipes.MANUFACTURE.get())) {
            if (holder.value().matchesSteps(copy)) {
                return holder.value().resultStack();
            }
        }
        return copy;
    }

    public boolean matchesSteps(ItemStack stack) {
        Item item = BuiltInRegistries.ITEM.getOptional(base).orElse(Items.AIR);
        if (!stack.is(item)) {
            return false;
        }
        return WorkpieceSteps.on(stack).steps().equals(steps);
    }

    public ItemStack resultStack() {
        Item item = BuiltInRegistries.ITEM.getOptional(result).orElse(Items.AIR);
        return new ItemStack(item, amount);
    }

    public ItemStack baseStack() {
        return new ItemStack(BuiltInRegistries.ITEM.getOptional(base).orElse(Items.AIR));
    }

    @Override
    public boolean matches(ManufactureInput input, Level level) {
        return matchesSteps(input.stack());
    }

    @Override
    public ItemStack assemble(ManufactureInput input, HolderLookup.Provider registries) {
        return resultStack();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return resultStack();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NeepRecipes.MANUFACTURE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return NeepRecipes.MANUFACTURE.get();
    }

    public record ManufactureStepSpec(String id, String resource) {
        public static final Codec<ManufactureStepSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(ManufactureStepSpec::id),
                Codec.STRING.fieldOf("resource").forGetter(ManufactureStepSpec::resource)
        ).apply(instance, ManufactureStepSpec::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ManufactureStepSpec> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, ManufactureStepSpec::id,
                ByteBufCodecs.STRING_UTF8, ManufactureStepSpec::resource,
                ManufactureStepSpec::new);

        public ItemStack icon() {
            ResourceLocation id = ResourceLocation.parse(resource);
            Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
            if (item != Items.AIR) {
                return new ItemStack(item);
            }
            Fluid fluid = BuiltInRegistries.FLUID.getOptional(id).orElse(null);
            if (fluid != null) {
                return new ItemStack(fluid.getBucket());
            }
            return ItemStack.EMPTY;
        }
    }

    private record IdRef(String id) {}

    private record ResultRef(String resource, int amount) {}
}
