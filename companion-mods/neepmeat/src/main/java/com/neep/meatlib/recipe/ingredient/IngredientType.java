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
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class IngredientType<R>
{
    // Easier than using an entire registry
    public static final Map<ResourceLocation, IngredientType<?>> ALL_TYPES = new HashMap<>();

    private final ResourceLocation id;
    private final Function<ResourceLocation, R> get;
    private final Function<R, ResourceLocation> getId;

    public IngredientType(ResourceLocation id, Function<ResourceLocation, R> get, Function<R, ResourceLocation> getId)
    {
        this.id = id;
        this.get = get;
        this.getId = getId;

        ALL_TYPES.put(id, this);
    }

    public Codec<R> resourceCodec()
    {
        return ResourceLocation.CODEC.xmap(this::get, this::getIdFor);
    }

    public static <R> IngredientType<R> of(ResourceLocation id, Registry<R> registry)
    {
        return new IngredientType<>(id, registry::get, registry::getKey);
    }

    public ResourceLocation getIdFor(R r)
    {
        return getId.apply(r);
    }

    @Nullable
    public R get(ResourceLocation id)
    {
        return get.apply(id);
    }

    public Optional<R> getOrEmpty(ResourceLocation id)
    {
        return Optional.ofNullable(get.apply(id));
    }

    public ResourceLocation id()
    {
        return id;
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(id);
    }

    public static <R> IngredientType<R> read(FriendlyByteBuf buf)
    {
        ResourceLocation id = buf.readResourceLocation();
        var type = (IngredientType<R>) ALL_TYPES.get(id);

        if (type == null)
            return Ingredients.empty();

        return type;
    }
}
