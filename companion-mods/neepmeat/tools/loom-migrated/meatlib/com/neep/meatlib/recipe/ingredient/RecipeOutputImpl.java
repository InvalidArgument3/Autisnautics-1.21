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
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neep.meatlib.MeatLib;
import com.neep.meatlib.util.MeatLibCodecs;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@ApiStatus.NonExtendable
@SuppressWarnings("UnstableApiUsage")
public class RecipeOutputImpl<T> implements RecipeOutput<T>
{
    private final T resource;
    private final UniformInt lootFunction;
    private final RandomSource random;
    private final float chance;
    private final IngredientType<T> type;
    private final @Nullable CompoundTag nbt;

    public RecipeOutputImpl(@NotNull T resource, long min, long max, float probability, IngredientType<T> type, @Nullable CompoundTag nbt)
    {
        this.resource = resource;
        this.type = type;
        this.lootFunction = UniformInt.of((int) min, (int) max);
        this.random = RandomSource.create();
        this.chance = probability;
        this.nbt = (nbt == null || nbt.isEmpty()) ? null : nbt;
    }

    public RecipeOutputImpl(@NotNull T resource, long amount, float probability, IngredientType<T> type, CompoundTag nbt)
    {
        this(resource, amount, amount, probability, type, nbt);
    }

    /// For any ingredient type
    public static Codec<RecipeOutput<?>> makeCodec()
    {
        return new MeatLibCodecs.JankDispatch<>("type", RecipeOutputs.REGISTRY.byNameCodec(), Function.identity()).codec();
    }

    public static <R> Codec<RecipeOutput<R>> makeCodec(IngredientType<R> type)
    {
        MapCodec<R> resourceCodec = type.resourceCodec().fieldOf("resource");

        MapCodec<Float> chanceCodec = Codec.FLOAT.optionalFieldOf("chance", 1.f);
        MapCodec<Long> amountCodec = Codec.LONG.fieldOf("amount");
        MapCodec<Long> minCodec = Codec.LONG.fieldOf("min");
        MapCodec<Long> maxCodec = Codec.LONG.fieldOf("max");
        MapCodec<CompoundTag> nbtCodec = CompoundTag.CODEC.optionalFieldOf("data", new CompoundTag());

        Codec<RecipeOutputImpl<R>> justAmount = RecordCodecBuilder.create(instance -> instance.group(
                        resourceCodec.forGetter(RecipeOutputImpl::resource),
                        chanceCodec.forGetter(RecipeOutputImpl::chance),
                        amountCodec.forGetter(RecipeOutputImpl::maxAmount),
                        nbtCodec.forGetter(RecipeOutputImpl::getNbt)
        ).apply(
                        instance, (resource, chance, amount, nbt) ->
                                new RecipeOutputImpl<>(resource, amount, chance, type, nbt)));

        Codec<RecipeOutputImpl<R>> minMax = RecordCodecBuilder.create(instance -> instance.group(
                        resourceCodec.forGetter(RecipeOutputImpl::resource),
                        chanceCodec.forGetter(RecipeOutputImpl::chance),
                        minCodec.forGetter(RecipeOutputImpl::minAmount),
                        maxCodec.forGetter(RecipeOutputImpl::maxAmount),
                        nbtCodec.forGetter(RecipeOutputImpl::getNbt)
                ).apply(
                        instance, (resource, chance, min, max, nbt) ->
                                new RecipeOutputImpl<>(resource, min, max, chance, type, nbt)));

        return Codec.either(justAmount, minMax)
                .xmap(MeatLibCodecs::collapseEither, Either::right)
                .<RecipeOutput<R>>xmap(Function.identity(), o -> (RecipeOutputImpl<R>) o); // Oof
    }

    public static <R> RecipeOutput<R> fromJson(JsonObject json)
    {
        if (json.has("type"))
        {
            ResourceLocation typeId = new ResourceLocation(GsonHelper.getAsString(json, "type"));
            IngredientType<R> type = (IngredientType<R>) IngredientType.ALL_TYPES.get(typeId);

            return fromJson(type, json);
        }
        else
        {
            throw new JsonParseException("Recipe input does not specify resource type.");
        }
    }

    public static <R> RecipeOutput<R> fromJson(IngredientType<R> type, JsonObject json)
    {
        return makeCodec(type)
                .decode(JsonOps.INSTANCE, json)
                .getOrThrow(false, MeatLib.LOGGER::error)
                .getFirst();
    }

    public static RecipeOutput<?> read(FriendlyByteBuf buf)
    {
        IngredientType<?> type = IngredientType.read(buf);
        return readAfterType(type, buf);
    }

    /// Legacy use of registry
    public static <R> RecipeOutputImpl<R> read(Registry<R> registry, FriendlyByteBuf buf)
    {
        IngredientType<R> type = IngredientType.read(buf);
        return readAfterType(type, buf);
    }

    public static <R> RecipeOutputImpl<R> read(IngredientType<R> type, FriendlyByteBuf buf)
    {
        var result = read(buf);
        if (!result.getType().equals(type))
            throw new IllegalArgumentException("Given ingredient type '%s' does not match found '%s'".formatted(type, result.getType()));

        return (RecipeOutputImpl<R>) result;
    }

    private static <R> RecipeOutputImpl<R> readAfterType(IngredientType<R> type, FriendlyByteBuf buf)
    {
        ResourceLocation id = buf.readResourceLocation();
        R resource = type.get(id);
        int min = buf.readVarInt();
        int max = buf.readVarInt();
        float chance = buf.readFloat();
        CompoundTag nbt = buf.readNbt();
        return new RecipeOutputImpl<>(resource, min, max, chance, type, nbt);
    }

    public static <R> void writeList(Registry<R> registry, List<RecipeOutput<R>> list, FriendlyByteBuf buf)
    {
        buf.writeVarInt(list.size());
        for (var element : list)
        {
            element.write(buf);
        }
    }

    public static <R> List<RecipeOutput<R>> readList(Registry<R> registry, FriendlyByteBuf buf)
    {
        List<RecipeOutput<R>> list = new ObjectArrayList<>();
        int size = buf.readVarInt();
        for (int i = 0; i < size; ++i)
        {
            list.add(read(registry, buf));
        }

        return list;
    }

    @Override
    public T resource()
    {
        return resource;
    }

    @Override
    public long randomAmount(float chanceMod)
    {
        float newChance = chance + chanceMod;

        // The expected amount should be (chance + chanceMod) * Ex(lootFunction)

        int rolls = Mth.floor(newChance);
        float bonus = newChance % 1;

        long amount = 0;
        for (int roll = 0; roll < rolls; ++roll)
        {
            amount += lootFunction.sample(random);
        }

        if (bonus > 0 && random.nextFloat() < bonus)
        {
            amount += lootFunction.sample(random);
        }

        return amount;
    }

    @Override
    public long maxAmount()
    {
        return lootFunction.getMaxValue();
    }

    @Override
    public long minAmount()
    {
        return lootFunction.getMinValue();
    }

    @Override
    public float chance()
    {
        return chance;
    }

    @Override
    public @Nullable CompoundTag getNbt()
    {
        return nbt;
    }

    @Override
    public IngredientType<T> getType()
    {
        return type;
    }

    @Override
    public <V extends TransferVariant<T>> boolean insertInto(Storage<V> storage, BiFunction<T, CompoundTag, V> of, float chanceModifier, TransactionContext transaction)
    {
        long amount = randomAmount(chanceModifier);

        if (amount == 0)
            return true;

        V variant = of.apply(resource, nbt);
        long inserted = storage.insert(variant, amount, transaction);
        return inserted == amount;
    }

    public void write(FriendlyByteBuf buf)
    {
        type.write(buf);
        buf.writeResourceLocation(type.getIdFor(resource));
        buf.writeVarInt(lootFunction.getMinValue());
        buf.writeVarInt(lootFunction.getMaxValue());
        buf.writeFloat(chance);
        buf.writeNbt(nbt);
    }
}
