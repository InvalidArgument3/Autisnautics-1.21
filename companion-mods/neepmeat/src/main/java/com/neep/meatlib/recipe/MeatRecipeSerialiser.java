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

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

public interface MeatRecipeSerialiser<T extends MeatlibRecipe<?>> extends RecipeSerializer<T>
{
    Codec<RecipeFunction<T>> codec();

    @Deprecated
    default T fromJson(ResourceLocation var1, JsonObject var2)
    {
        return codec().parse(JsonOps.INSTANCE, var2).getOrThrow(true, s ->
        {
            throw new JsonParseException(s);
        }).apply(var1);
    }

    T fromNetwork(ResourceLocation id, FriendlyByteBuf buf);

    void write(FriendlyByteBuf buf, T recipe);

    /**
     * One codec per serializer. The 1.20 helpers still take the recipe id, so the
     * id is written at the front of this payload and read back into {@link #fromNetwork}.
     */
    @Override
    default StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return StreamCodec.of(
                (buf, recipe) -> {
                    buf.writeResourceLocation(recipe.getId());
                    write(buf, recipe);
                },
                buf -> fromNetwork(buf.readResourceLocation(), buf));
    }

    // For sooth(ish) transitions between 1.21 and 1.20
    // The ID parameter is removed past 1.21
    interface RecipeFunction<T>
    {
        T apply(ResourceLocation id);
    }
}
