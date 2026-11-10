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
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents a required quantity of an ingredient, supplied either by a tag or a specific resource.
 *
 * @param <R> A resource with a corresponding registry, such as Item or Fluid
 */
@SuppressWarnings("UnstableApiUsage")
public class RegistryRecipeInput<R> implements RecipeInput<R, StorageView<? extends TransferVariant<?>>>
{
    protected final RegistryRecipeInputSerialiser<R> serialiser;
    protected final IngredientType<?> type;
    protected final long amount;
    protected final NbtMatcher nbtMatcher;
    // Only one entry at the moment.
    protected Entry<R> entry;
    @Nullable
    protected R[] matchingObjects;

    protected RegistryRecipeInput(Entry<R> entry, long amount, RegistryRecipeInputSerialiser<R> serialiser, IngredientType<?> type)
    {
        this(entry, amount, serialiser, type, NbtMatcher.ANY);
    }

    protected RegistryRecipeInput(Entry<R> entry, long amount, RegistryRecipeInputSerialiser<R> serialiser, IngredientType<?> type, NbtMatcher nbtMatch)
    {
        this.entry = entry;
        this.amount = amount;
        this.serialiser = serialiser;
        this.type = type;
        this.nbtMatcher = nbtMatch;
    }

    public static <R> RegistryRecipeInput<R> fromBuffer(FriendlyByteBuf buf)
    {
        ResourceLocation type = ResourceLocation.parse(buf.readUtf());
        Serialiser<?, ?, ?> serialiser = RecipeInputs.SERIALISERS.get(type);
        if (serialiser instanceof RegistryRecipeInputSerialiser<?> serialiser1)
        {
            return (RegistryRecipeInput<R>) serialiser1.fromBuffer(buf);
        }
        else
        {
            throw new IllegalStateException("Error reading recipe packet: invalid type");
        }
    }

    // TODO: move somewhere else or something
    public static <R> RegistryRecipeInput<R> fromJson(RegistryRecipeInputSerialiser<R> serialiser, JsonObject json)
    {
        return serialiser.fromJson(json);
    }

    /**
     * Requires a 'type' field in the JSON object.
     *
     * @param json Recipe JSON object
     * @return A RecipeInput based on the detected resource type
     */
    public static RegistryRecipeInput<?> fromJson(JsonObject json)
    {
        ResourceLocation type;
        Serialiser<?, ?, ?> serialiser;
        if (json.has("type"))
        {
            type = ResourceLocation.fromNamespaceAndPath(GsonHelper.getAsString(json, "type"));
            serialiser = RecipeInputs.SERIALISERS.get(type);
        }
        else
        {
            throw new JsonParseException("Recipe input does not specify resource type.");
        }

        if (serialiser == null)
            throw new JsonParseException("Unknown resource type '" + type + "'");

        return (RegistryRecipeInput<?>) serialiser.fromJson(json);
    }

    public <V extends TransferVariant<R>> boolean extract(Storage<V> storage, Function<R, V> function, TransactionContext transaction)
    {
        @Nullable R item = getFirstMatching(storage, transaction).orElse(null);
        if (item == null)
            return false;

        long extracted = storage.extract(function.apply(item), amount(), transaction);
        return extracted == amount();
    }

    @Override
    public RegistryRecipeInputSerialiser<R> getSerialiser()
    {
        return serialiser;
    }

    @Override
    public IngredientType<?> getType()
    {
        return type;
    }

    @Override
    public void write(FriendlyByteBuf buf)
    {
        serialiser.write(buf, this);
    }

    public long amount()
    {
        return amount;
    }

    @Override
    public boolean isEmpty()
    {
        return this.amount == 0 || this == RecipeInputs.EMPTY;
    }

    public void cacheMatching()
    {
        if (this.matchingObjects == null)
        {
            this.matchingObjects = (R[]) (entry.getMatching().stream()).distinct().toArray();
        }
    }

    @Override
    public boolean test(StorageView<? extends TransferVariant<?>> storageView)
    {
        return test(storageView.getResource(), storageView.getAmount());
    }

    public boolean testStorage(Storage<? extends TransferVariant<?>> storage)
    {
        for (StorageView<? extends TransferVariant<?>> view : storage)
        {
            // Not very efficient, but hopefully this won't be used on storages with many slots too often.
            if (test(view))
                return true;
        }
        return false;
    }

    @Override
    public boolean test(TransferVariant<?> variant, long amount)
    {
        cacheMatching();
        if (!nbtMatcher.test(variant.getNbt()))
            return false;

        return Arrays.stream(matchingObjects).anyMatch(o -> variant.getObject().equals(o)) && amount >= amount();
    }

    public Optional<R> getFirstMatching(StorageView<? extends TransferVariant<?>> view)
    {
        cacheMatching();
        if (!nbtMatcher.test(view.getResource().getNbt()))
            return Optional.empty();

        return Arrays.stream(matchingObjects).filter(r -> view.getResource().getObject().equals(r)).findFirst();
    }

    public <V extends TransferVariant<R>> Optional<V> getFirstMatching(StorageView<? extends TransferVariant<R>> view, BiFunction<R, CompoundTag, V> of)
    {
        cacheMatching();
        var resource = view.getResource();

        if (!nbtMatcher.test(resource.getNbt()))
            return Optional.empty();

        for (R object : matchingObjects)
        {
            if (resource.getObject().equals(object))
                return Optional.of(of.apply(resource.getObject(), resource.getNbt()));
        }

        return Optional.empty();
    }

    public Optional<R> getFirstMatching(Storage<? extends TransferVariant<?>> storage, TransactionContext transaction)
    {
        cacheMatching();
        for (StorageView<? extends TransferVariant<?>> view : storage)
        {
//            Optional<R> optional = Arrays.stream(matchingObjects).filter(r -> view.getResource().getObject().equals(r)).findFirst();
            Optional<R> optional = getFirstMatching(view);

            if (optional.isPresent())
                return optional;
        }
        return Optional.empty();
    }

    public <V extends TransferVariant<R>> Optional<V> getFirstMatching(Storage<V> storage, BiFunction<R, CompoundTag, V> func)
    {
        cacheMatching();
        for (StorageView<V> view : storage)
        {
//            Optional<R> optional = Arrays.stream(matchingObjects).filter(r -> view.getResource().getObject().equals(r)).findFirst();
            Optional<V> optional = getFirstMatching(view, func);

            if (optional.isPresent())
                return optional;
        }
        return Optional.empty();
    }

    public Collection<R> getAll()
    {
        return entry.getMatching();
    }

    @Override
    public R[] getMatching()
    {
        return matchingObjects;
    }

    public NbtMatcher nbtMatcher()
    {
        return nbtMatcher;
    }

    public boolean resourcesMatch(RegistryRecipeInput<R> other)
    {
        return Objects.equals(serialiser, other.serialiser)
                && Objects.equals(type, other.type)
                && Objects.equals(entry, other.entry)
                && Objects.equals(nbtMatcher.getNbt(), other.nbtMatcher.getNbt());
    }

    public interface Entry<T>
    {

        Entry<Object> EMPTY = () -> Collections.EMPTY_SET;

        Collection<T> getMatching();
    }

    public static class TagEntry<T> implements Entry<T>
    {
        protected TagKey<T> tag;

        TagEntry(TagKey<T> tag)
        {
            this.tag = tag;
        }

        // TODO: Find a way of getting registry from RegistryKey
        @SuppressWarnings("unchecked")
        public static <T> Registry<T> getRegistry(ResourceKey<? extends Registry<T>> key)
        {
            if (BuiltInRegistries.ITEM.key().equals(key))
            {
                return (Registry<T>) BuiltInRegistries.ITEM;
            }
            else if (BuiltInRegistries.FLUID.key().equals(key))
            {
                return (Registry<T>) BuiltInRegistries.FLUID;
            }
            throw new NotImplementedException();
        }

        @Override
        public Collection<T> getMatching()
        {
            ArrayList<T> list = new ArrayList<>();
            Registry<T> registry = getRegistry(tag.registry());
            for (Holder<T> registryEntry : registry.getTagOrEmpty(this.tag))
            {
                list.add(registryEntry.value());
            }
            return list;
        }

        public TagKey<T> getTag()
        {
            return tag;
        }

        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof TagEntry<?> tagEntry))
                return false;

            return Objects.equals(tag, tagEntry.tag);
        }

        @Override
        public int hashCode()
        {
            return Objects.hashCode(tag);
        }
    }

    public static class ResourceEntry<T> implements Entry<T>
    {
        protected T object;

        public ResourceEntry(T resource)
        {
            this.object = resource;
        }

        public T getObject()
        {
            return object;
        }

        @Override
        public Collection<T> getMatching()
        {
            return List.of(object);
        }

        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof ResourceEntry<?> that))
                return false;
            return Objects.equals(object, that.object);
        }

        @Override
        public int hashCode()
        {
            return Objects.hashCode(object);
        }
    }
}