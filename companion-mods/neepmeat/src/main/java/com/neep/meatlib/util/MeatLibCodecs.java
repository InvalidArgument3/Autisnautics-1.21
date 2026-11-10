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

package com.neep.meatlib.util;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.apache.commons.lang3.NotImplementedException;

import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class MeatLibCodecs
{
    public static final Codec<Item> CHECKED_ITEM = ResourceLocation.CODEC.comapFlatMap(
            checkDefault(ResourceLocation.fromNamespaceAndPath("air"), Items.AIR, BuiltInRegistries.ITEM), BuiltInRegistries.ITEM::getKey);

    public static final Codec<Fluid> CHECKED_FLUID = ResourceLocation.CODEC.comapFlatMap(
            checkDefault(ResourceLocation.fromNamespaceAndPath("empty"), Fluids.EMPTY, BuiltInRegistries.FLUID), BuiltInRegistries.FLUID::getKey);

    // NbtCompound.CODEC causes information loss when writing to JSON. All numbers (shorts, ints, bytes) are converted to the JSON number type.
    // Reading the result discards the type of the tags.
    // This thing also doesn't work as there's no way of writing 123s or 123b in JSON (without using strings).
//    Codec<String> NBT_STRING_CODEC = Codec.PASSTHROUGH.comapFlatMap(dynamic ->
//            {
//                JsonElement jsonElement = dynamic.convert(JsonOps.INSTANCE).getValue();
//                return jsonElement instanceof JsonElement ? DataResult.success(jsonElement) : DataResult.error(() -> "Not JSON: " + jsonElement);
//            }, json -> new Dynamic<>(JsonOps.INSTANCE, json))
//            .xmap(JsonElement::getAsString, JsonParser::parseString)
//            .xmap(StringNbtReader
//            ;

    public static <T> Function<ResourceLocation, DataResult<T>> checkDefault(ResourceLocation defaultId, T defaultValue, Registry<T> registry)
    {
        return id ->
        {
            T entry = registry.get(id);
            if (entry == defaultValue && !id.equals(defaultId))
                return DataResult.error(() -> "id " + id + " not found in registry " + registry.key().location());

            return DataResult.success(entry);
        };
    }

    public static <T, R> R notImplemented(T t)
    {
        throw new NotImplementedException("My life is too short for this");
    }

    public static <T, O> RecordCodecBuilder<O, T> decodeOnly(MapCodec<T> mapCodec)
    {
        return mapCodec.forGetter(MeatLibCodecs::notImplemented);
    }

    public static <T> T collapseEither(Either<? extends T, ? extends T> either)
    {
        return either.left().isPresent() ? either.left().get() : either.right().orElseThrow();
    }

    public static <T> Function<? super Either<? extends T, ? extends T>, ? extends DataResult<? extends T>> collapseEither(String error)
    {
        return e ->
        {
            if (e.left().isPresent())
            {
                return DataResult.success(e.left().get());
            }
            else if (e.right().isPresent())
            {
                return DataResult.success(e.right().get());
            }

            return DataResult.error(() -> error);
        };
    }

    /// Like KeyDispatchCodec except it expects the type element to be in the same map as the value elements.
    /// Should probably be replaced with {@link com.mojang.serialization.codecs.KeyDispatchCodec} with assumeMap=true.
    public static class JankDispatch<T, S> extends MapCodec<T>
    {
        private final String typeKey;
        private final Codec<S> codec;
        private final Function<S, Codec<T>> extractCodec;

        public JankDispatch(String typeKey, Codec<S> keyCodec, Function<S, Codec<T>> extractCodec)
        {
            this.typeKey = typeKey;
            this.codec = keyCodec;
            this.extractCodec = extractCodec;
        }

        @Override
        public <T1> DataResult<T> decode(DynamicOps<T1> ops, MapLike<T1> input)
        {
            final T1 elementName = input.get(typeKey);
            if (elementName == null)
            {
                return DataResult.error(() -> "Input does not contain a key [" + typeKey + "]: " + input);
            }

            return codec.decode(ops, elementName).flatMap(type ->
            {
                final DataResult<? extends Decoder<? extends T>> elementDecoder = DataResult.success(extractCodec.apply(type.getFirst()));
                return elementDecoder.flatMap(c ->
                        c.decode(ops, ops.createMap(input.entries())).map(Pair::getFirst));
            });
        }

        @Override
        public <T1> Stream<T1> keys(DynamicOps<T1> ops)
        {
            // There's no way of knowing which keys the value decoder will have without the key element
            return Stream.of(typeKey).map(ops::createString);
        }

        @Override
        public <T1> RecordBuilder<T1> encode(T input, DynamicOps<T1> ops, RecordBuilder<T1> prefix)
        {
            return prefix.withErrorsFrom(DataResult.error(() -> "JankDispatch::encode not implemented"));
        }
    }
}
