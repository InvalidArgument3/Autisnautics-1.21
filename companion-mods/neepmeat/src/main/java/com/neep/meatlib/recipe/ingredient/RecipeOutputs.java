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
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.ParamCodecs;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import com.neep.meatlib.transfer.ItemVariant;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

public class RecipeOutputs
{
    public static final Registry<Codec<RecipeOutput<?>>> REGISTRY = FabricRegistryBuilder.<Codec<RecipeOutput<?>>>createSimple(ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MeatLib.NAMESPACE, "recipe_output")))
            .buildAndRegister();

    public static final Codec<RecipeOutput<Item>> ITEM = RecipeOutputImpl.makeCodec(Ingredients.ITEM);
    public static final Codec<RecipeOutput<Fluid>> FLUID = RecipeOutputImpl.makeCodec(Ingredients.FLUID);
    public static final Codec<RecipeOutput<?>> ANY = RecipeOutputImpl.makeCodec();

    public static final ParamCodec<RecipeOutputImpl<Item>> ITEM_PARAM = ParamCodec.of(ParamCodecs.as(RecipeOutputImpl.class), RecipeOutputImpl::write, b -> RecipeOutputImpl.read(BuiltInRegistries.ITEM, b));
    public static final ParamCodec<RecipeOutputImpl<Fluid>> FLUID_PARAM = ParamCodec.of(ParamCodecs.as(RecipeOutputImpl.class), RecipeOutputImpl::write, b -> RecipeOutputImpl.read(BuiltInRegistries.FLUID, b));

    public static RecipeOutputImpl<Item> of(ItemVariant variant, long min, long max, float chance)
    {
        return new RecipeOutputImpl<>(variant.getItem(), min, max, chance, Ingredients.ITEM, variant.getNbt());
    }

    public static void init()
    {
        Registry.register(REGISTRY, Ingredients.ITEM.id(), (Codec<RecipeOutput<?>>) (Object) ITEM);
        Registry.register(REGISTRY, Ingredients.FLUID.id(), (Codec<RecipeOutput<?>>) (Object) FLUID);
    }
}
