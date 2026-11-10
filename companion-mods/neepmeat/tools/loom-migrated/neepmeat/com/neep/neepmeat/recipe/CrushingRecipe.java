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

package com.neep.neepmeat.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neep.meatlib.recipe.MeatRecipeSerialiser;
import com.neep.meatlib.recipe.MeatRecipeType;
import com.neep.meatlib.recipe.MeatlibRecipe;
import com.neep.meatlib.util.MeatLibCodecs;
import com.neep.meatlib.recipe.ingredient.*;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.grinder.CrusherRecipeContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class CrushingRecipe implements MeatlibRecipe<CrusherRecipeContext>
{
    protected ResourceLocation id;
    protected RegistryRecipeInput<Item> itemInput;
    protected RecipeOutput<Item> itemOutput;
    protected RecipeOutput<Item> extraOutput;
    protected float experience;
    protected int processTime;

    public CrushingRecipe(ResourceLocation id, RegistryRecipeInput<Item> itemInput, RecipeOutput<Item> itemOutput, RecipeOutput<Item> extraOutput, float experience, int processTime)
    {
        this.itemInput = itemInput;
        this.itemOutput = itemOutput;
        this.extraOutput = extraOutput;
        this.experience = experience;
        this.processTime = processTime;
        this.id = id;
    }

    public boolean destroy()
    {
        return false;
    }

    @Override
    public boolean matches(CrusherRecipeContext inventory)
    {
        return itemInput.testStorage(inventory.getInputStorage());
    }

    public RegistryRecipeInput<Item> getItemInput()
    {
        return itemInput;
    }

    public RecipeOutput<Item> getItemOutput()
    {
        return itemOutput;
    }

    public RecipeOutput<Item> getAuxOutput()
    {
        return extraOutput;
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    public MeatRecipeSerialiser<?> getSerializer()
    {
        return NMrecipeTypes.GRINDING_SERIALIZER;
    }

    @Override
    public MeatRecipeType<?> getType()
    {
        return NMrecipeTypes.GRINDING;
    }

    public int getTime()
    {
        return processTime;
    }

    @Override
    public boolean takeInputs(CrusherRecipeContext storage, TransactionContext transaction)
    {
        try (Transaction inner = transaction.openNested())
        {
            Optional<ItemVariant> item = itemInput.getFirstMatching(storage.getInputStorage(), ItemVariant::of);
            if (item.isEmpty())
            {
                inner.abort();
                return false;
            }

            long extracted = storage.getInputStorage().extract(item.get(), itemInput.amount(), inner);
            if (extracted == itemInput.amount())
            {
                inner.commit();
                return true;
            }
            inner.abort();
        }
        return false;
    }

    @Override
    public boolean ejectOutputs(CrusherRecipeContext context, TransactionContext transaction)
    {
        try (Transaction inner = transaction.openNested())
        {
            // Only apply the chance modifier to the extra output
            boolean bl1 = itemOutput.insertInto(context.getOutputStorage(), ItemVariant::of, inner);
            boolean bl2 = extraOutput == null || extraOutput.insertInto(context.getExtraStorage(), ItemVariant::of, context.getChanceMod(), inner);
            boolean bl3 = context.getXpStorage().insert(experience, transaction) == experience;

            if (bl1 && bl2 && bl3)
            {
                inner.commit();
                return true;
            }
            inner.abort();
        }
        return false;
    }

    public static class Serializer<T extends CrushingRecipe> implements MeatRecipeSerialiser<T>
    {
        private final RecipeFactory<T> factory;
        private final DestroyRecipeFactory<T> destroyFactory;
        private final int processTIme;
        private final Codec<RecipeFunction<T>> codec;

        public Serializer(RecipeFactory<T> recipeFactory, DestroyRecipeFactory<T> destroyFactory, int processTime)
        {
            this.factory = recipeFactory;
            this.destroyFactory = destroyFactory;
            this.processTIme = processTime;

            Codec<RecipeFunction<T>> c1 = Codec.BOOL.<RecipeFunction<T>>xmap(b -> destroyFactory::create, MeatLibCodecs::notImplemented)
                    .fieldOf("destroy").codec();

            Codec<RecipeFunction<T>> c2 = RecordCodecBuilder.create(instance -> instance.group(
                    RecipeInputs.ITEM.codec().fieldOf("input").forGetter(MeatLibCodecs::notImplemented),
                    RecipeOutputs.ITEM.fieldOf("output").forGetter(MeatLibCodecs::notImplemented),
                    RecipeOutputs.ITEM.optionalFieldOf("extra").forGetter(MeatLibCodecs::notImplemented),
                    Codec.FLOAT.optionalFieldOf("experience", 0f).forGetter(MeatLibCodecs::notImplemented),
                    Codec.INT.optionalFieldOf("processtime", this.processTIme).forGetter(MeatLibCodecs::notImplemented)
            ).apply(instance, (i, o, e, ex, t) ->
                    id -> factory.create(id, i, o, e.orElse(null), ex, t)));

            this.codec = Codec.either(c1, c2).xmap(MeatLibCodecs::collapseEither, Either::left);
        }

        @Override
        public Codec<RecipeFunction<T>> codec()
        {
            return codec;
        }

        @Override
        public T fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
        {
            // Ignore everything else
            if (buf.readBoolean())
                return destroyFactory.create(id);

            RegistryRecipeInput<Item> itemInput = RegistryRecipeInput.fromBuffer(buf);
            RecipeOutputImpl<Item> itemOutput = RecipeOutputImpl.read(BuiltInRegistries.ITEM, buf);

            RecipeOutputImpl<Item> extraOutput = null;
            if (buf.readBoolean())
            {
                extraOutput = RecipeOutputImpl.read(BuiltInRegistries.ITEM, buf);
            }

            float experience = buf.readFloat();
            int time = buf.readVarInt();

            return this.factory.create(id, itemInput, itemOutput, extraOutput, experience, time);
        }

        @Override
        public void write(FriendlyByteBuf buf, CrushingRecipe recipe)
        {
            if (recipe.destroy())
            {
                buf.writeBoolean(true);
                return;
            }
            else
            {
                buf.writeBoolean(false);
            }

            recipe.itemInput.write(buf);
            recipe.itemOutput.write(buf);

            // Include extra only if present
            if (recipe.extraOutput != null)
            {
                buf.writeBoolean(true);
                recipe.extraOutput.write(buf);
            }
            else buf.writeBoolean(false);

            buf.writeFloat(recipe.experience);
            buf.writeVarInt(recipe.processTime);
        }

        @FunctionalInterface
        public interface RecipeFactory<T extends CrushingRecipe>
        {
            T create(ResourceLocation var1, RegistryRecipeInput<Item> in, RecipeOutput<Item> out, @Nullable RecipeOutput<Item> eOut, float xp, int time);
        }

        public interface DestroyRecipeFactory<T extends CrushingRecipe>
        {
            T create(ResourceLocation id);
        }
    }

    // A recipe that destroys any input item, for use in the Large Crusher.
    public static class DestroyRecipe extends CrushingRecipe
    {
        public DestroyRecipe(ResourceLocation id)
        {
            super(id, RecipeInputs.empty(), RecipeOutput.empty(), RecipeOutput.empty(), 0, 40);
        }

        @Override
        public boolean destroy()
        {
            return true;
        }

        @Override
        public boolean matches(CrusherRecipeContext inventory)
        {
            return false;
        }

        @Override
        public boolean takeInputs(CrusherRecipeContext storage, TransactionContext transaction)
        {
            var it = storage.getInputStorage().nonEmptyIterator();
            if (it.hasNext())
            {
                StorageView<ItemVariant> view = it.next();
                view.extract(view.getResource(), 1, transaction);
                return true;
            }
            return false;
        }

        @Override
        public boolean ejectOutputs(CrusherRecipeContext context, TransactionContext transaction)
        {
            return true;
        }
    }
}
