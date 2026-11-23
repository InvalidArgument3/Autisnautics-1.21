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

package com.neep.meatlib.recipe.ingredient;

import com.mojang.serialization.Codec;
import com.neep.meatlib.MeatLib;
import com.neep.meatlib.recipe.ingredient.entity.EntityRecipeInput;
import com.neep.meatlib.util.MeatLibCodecs;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import com.neep.meatlib.transfer.ItemVariant;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

public class RecipeInputs
{
    public static final Registry<RegistryRecipeInput.Serialiser<?, ?, ?>> SERIALISERS = FabricRegistryBuilder.createDefaulted(
            (Class<RegistryRecipeInput.Serialiser<?, ?, ?>>) (Object) RegistryRecipeInput.Serialiser.class,
            ResourceLocation.fromNamespaceAndPath(MeatLib.NAMESPACE, "recipe_input"),
            Ingredients.EMPTY_ID).buildAndRegister();

    public static Codec<RecipeInput<?, ?>> ANY = new MeatLibCodecs.JankDispatch<>("type", RecipeInputs.SERIALISERS.byNameCodec(), e -> (Codec<RecipeInput<?, ?>>) e.codec()).codec();

    public static final RegistryRecipeInputSerialiser<Fluid> FLUID = Registry.register(SERIALISERS, Ingredients.FLUID.id(), new RegistryRecipeInputSerialiser<>(BuiltInRegistries.FLUID, Ingredients.FLUID));
    public static final RegistryRecipeInputSerialiser<Item> ITEM = Registry.register(SERIALISERS, Ingredients.ITEM.id(), new RegistryRecipeInputSerialiser<>(BuiltInRegistries.ITEM, Ingredients.ITEM));
    public static final EntityRecipeInput.EntityInputSerialiser ENTITY = Registry.register(SERIALISERS, Ingredients.ENTITY_MUTATE.id(), new EntityRecipeInput.EntityInputSerialiser());

    public static final RegistryRecipeInput<Object> EMPTY = new RegistryRecipeInput<>(RegistryRecipeInput.Entry.EMPTY, 0, RegistryRecipeInputSerialiser.EMPTY, Ingredients.EMPTY);

    public static <T> RegistryRecipeInput<T> empty()
    {
        return (RegistryRecipeInput<T>) EMPTY;
    }

    public static void init()
    {

    }

    public static RegistryRecipeInput<Item> of(Item item, long amount)
    {
        return new RegistryRecipeInput<>(new RegistryRecipeInput.ResourceEntry<>(item), amount, ITEM, Ingredients.ITEM);
    }

    public static RegistryRecipeInput<Item> of(ItemVariant item, long amount)
    {
        return new RegistryRecipeInput<>(new RegistryRecipeInput.ResourceEntry<>(item.getItem()), amount, ITEM, Ingredients.ITEM, new NbtMatcher(item.getNbt()));
    }
}
