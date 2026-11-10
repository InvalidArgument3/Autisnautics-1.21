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

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.ParamCodecs;
import com.neep.meatlib.transfer.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class RegistryRecipeInputSerialiser<R> implements RecipeInput.Serialiser<R, StorageView<? extends TransferVariant<?>>, RegistryRecipeInput<R>>
{
    // Well, this is a bit cursed.
    public static final RegistryRecipeInputSerialiser<Object> EMPTY = Registry.register(
            RecipeInputs.SERIALISERS, Ingredients.EMPTY_ID, new RegistryRecipeInputSerialiser<Object>((Registry<Object>) (Registry) BuiltInRegistries.ITEM, null)
            {
                @Override
                public RegistryRecipeInput<Object> fromBuffer(FriendlyByteBuf buf)
                {
                    return RecipeInputs.EMPTY;
                }

                @Override
                public IngredientType<Object> getType()
                {
                    return Ingredients.EMPTY;
                }

                @Override
                public Codec<RegistryRecipeInput<Object>> codec()
                {
                    return Codec.unit(RecipeInputs.EMPTY);
                }
            });

    private final Registry<R> registry;
    private final IngredientType<R> type;
    private final Codec<RegistryRecipeInput<R>> codec;
    private final ParamCodec<RegistryRecipeInput<R>> paramCodec;

    public RegistryRecipeInputSerialiser(Registry<R> registry, IngredientType<R> type)
    {
        this.registry = registry;
        this.type = type;
        this.codec = makeCodec(registry, this, type);

        paramCodec = ParamCodec.of(ParamCodecs.as(RegistryRecipeInput.class), RegistryRecipeInput::write, RegistryRecipeInput::fromBuffer);
    }

    public static <R> Codec<RegistryRecipeInput<R>> makeCodec(Registry<R> registry, RegistryRecipeInputSerialiser<R> serialiser, IngredientType<R> type)
    {
        MapCodec<Long> amountCodec = Codec.LONG.fieldOf("amount");

        MapCodec<NbtMatcher> matcherCodec = NbtMatcher.CODEC.optionalFieldOf("data", NbtMatcher.ANY);

        // Exact resource
        Codec<RegistryRecipeInput<R>> codec1 = RecordCodecBuilder.create(instance -> instance.group(
                registry.byNameCodec().xmap(RegistryRecipeInput.ResourceEntry::new, RegistryRecipeInput.ResourceEntry::getObject).fieldOf("resource").forGetter(i -> null),
                amountCodec.forGetter(RegistryRecipeInput::amount),
                matcherCodec.forGetter(RegistryRecipeInput::nbtMatcher)
        ).apply(instance, (c, a, n) -> new RegistryRecipeInput<>(c, a, serialiser, type, n)));

        // Tag
        Codec<RegistryRecipeInput<R>> codec2 = RecordCodecBuilder.create(instance -> instance.group(
                RecipeInput.tagKeyCodec(registry).xmap(RegistryRecipeInput.TagEntry::new, RegistryRecipeInput.TagEntry::getTag).fieldOf("tag").forGetter(i -> null),
                amountCodec.forGetter(RegistryRecipeInput::amount),
                matcherCodec.forGetter(RegistryRecipeInput::nbtMatcher)
        ).apply(instance, (c, a, n) -> new RegistryRecipeInput<>(c, a, serialiser, type, n)));

        // TODO: work out how to properly encode
        // WITHOUT ADDING ANOTHER FUCKING REGISTRY
        return Codec.either(codec1, codec2).xmap(
                e ->
                {
                    if (e.left().isPresent())
                        return e.left().get();
                    else return e.right().orElseThrow();
                }, Either::left);
    }

    public RegistryRecipeInput<R> fromBuffer(FriendlyByteBuf buf)
    {
        RegistryRecipeInput.Entry<R> entry = null;
        boolean isTag = buf.readBoolean();
        if (isTag)
        {
            ResourceLocation id = buf.readResourceLocation();
            TagKey<R> tagKey = TagKey.create(registry.key(), id);
            entry = new RegistryRecipeInput.TagEntry<>(tagKey);
        }
        else
        {
            ResourceLocation id = buf.readResourceLocation();
            R r = registry.get(id);
            entry = new RegistryRecipeInput.ResourceEntry<>(r);
        }
        long amount = buf.readVarLong();

        NbtMatcher matcher = NbtMatcher.read(buf);

        return new RegistryRecipeInput<>(entry, amount, this, type, matcher);
    }

    @Override
    public void write(FriendlyByteBuf buf, RegistryRecipeInput<R> input)
    {
        RecipeInput.Serialiser.super.write(buf, input);

        // This jank is here to avoid making another registry.
        boolean isTag = input.entry instanceof RegistryRecipeInput.TagEntry;
        buf.writeBoolean(isTag);
        if (isTag)
        {
            buf.writeResourceLocation(((RegistryRecipeInput.TagEntry<R>) input.entry).tag.location());
        }
        else
        {
            buf.writeResourceLocation(type.getIdFor(((RegistryRecipeInput.ResourceEntry<R>) input.entry).getObject()));
        }

        buf.writeVarLong(input.amount());

        input.nbtMatcher.write(buf);
    }

    @Override
    public IngredientType<R> getType()
    {
        return type;
    }

    public Codec<RegistryRecipeInput<R>> codec()
    {
        return codec;
    }

    public ParamCodec<RegistryRecipeInput<R>> paramCodec()
    {
        return paramCodec;
    }
}
