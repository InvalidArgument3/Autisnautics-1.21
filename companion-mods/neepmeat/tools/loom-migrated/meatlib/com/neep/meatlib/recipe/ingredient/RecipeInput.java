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

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.neep.meatlib.MeatLib;
import java.util.function.Predicate;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public interface RecipeInput<T, C> extends Predicate<C>
{
    Serialiser<T, C, ? extends RecipeInput<T, C>> getSerialiser();
    IngredientType<?> getType();
    void write(FriendlyByteBuf buf);
    boolean isEmpty();

    interface Serialiser<R, C, I extends RecipeInput<R, C>>
    {
        default I fromJson(JsonObject json)
        {
            return codec().decode(JsonOps.INSTANCE, json).getOrThrow(false, MeatLib.LOGGER::error).getFirst();
        }

        I fromBuffer(FriendlyByteBuf buf);

        Codec<I> codec();

        default void write(FriendlyByteBuf buf, I input)
        {
            buf.writeUtf(getType().id().toString());
        }

        IngredientType<R> getType();
    }

    static <T> Codec<TagKey<T>> tagKeyCodec(Registry<T> registry)
    {
        return ResourceLocation.CODEC.xmap(i -> TagKey.create(registry.key(), i), TagKey::location);
    }
}
