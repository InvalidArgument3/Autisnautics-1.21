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

import com.neep.meatlib.MeatLib;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

public class Ingredients
{
//    public static final Registry<IngredientType<?>> REGISTRY = FabricRegistryBuilder.<IngredientType<?>>createSimple(
//            RegistryKey.ofRegistry(new Identifier(MeatLib.NAMESPACE, "ingredient_type"))).buildAndRegister();

    public static final ResourceLocation EMPTY_ID = ResourceLocation.fromNamespaceAndPath(MeatLib.NAMESPACE, "empty");

    public static final IngredientType<Object> EMPTY = new IngredientType<>(EMPTY_ID, id -> null, r -> EMPTY_ID);

    public static final IngredientType<Item> ITEM = IngredientType.of(BuiltInRegistries.ITEM.key().location(), BuiltInRegistries.ITEM);
    public static final IngredientType<Fluid> FLUID = IngredientType.of(BuiltInRegistries.FLUID.key().location(), BuiltInRegistries.FLUID);
    public static final IngredientType<EntityType<?>> ENTITY_MUTATE = IngredientType.of(BuiltInRegistries.ENTITY_TYPE.key().location(), BuiltInRegistries.ENTITY_TYPE);

    public static <R> IngredientType<R> empty()
    {
        // The empty instance's methods are trivial, so it should be safe to call them without incorrect casts
        return (IngredientType<R>) EMPTY;
    }
}
