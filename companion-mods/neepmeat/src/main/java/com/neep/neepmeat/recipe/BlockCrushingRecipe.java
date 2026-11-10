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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neep.meatlib.recipe.MeatRecipeSerialiser;
import com.neep.meatlib.recipe.MeatRecipeType;
import com.neep.meatlib.recipe.ingredient.RecipeInputs;
import com.neep.meatlib.recipe.ingredient.RecipeOutput;
import com.neep.meatlib.util.MeatLibCodecs;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.processing.BlockCrushingRegistry;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.grinder.CrusherRecipeContext;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public class BlockCrushingRecipe extends AdvancedCrushingRecipe
{
    private final long mainAmount;
    private final long extraAmount;
    private final float outputChance;

    public static final BlockCrushingRecipe INSTANCE = new BlockCrushingRecipe(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block_crushing/instance"),
            1, 1, 0.5f);

    public BlockCrushingRecipe(ResourceLocation id, long mainAmount, long extraAmount, float outputChance)
    {
        super(id, RecipeInputs.empty(), RecipeOutput.empty(), RecipeOutput.empty(), 5, 40);
        this.mainAmount = mainAmount;
        this.extraAmount = extraAmount;
        this.outputChance = outputChance;
    }

    @Nullable
    protected BlockCrushingRegistry.Entry getFromInput(ItemVariant input)
    {
        return BlockCrushingRegistry.INSTANCE.getFromInputBasic(input);
    }

    public boolean matches(CrusherRecipeContext storage)
    {
        for (var view : storage.getInputStorage())
        {
            if (getFromInput(view.getResource()) != null)
                return true;
        }
        return super.matches(storage);
    }

    public boolean takeInputs(CrusherRecipeContext storage, TransactionContext transaction)
    {
        for (var view : storage.getInputStorage())
        {
            ItemVariant resource = view.getResource();
            BlockCrushingRegistry.Entry entry = getFromInput(resource);
            if (entry != null)
            {
                Item item = resource.getItem();
                try (Transaction inner = transaction.openNested())
                {
                    long extracted = storage.getInputStorage().extract(ItemVariant.of(item), entry.input().amount(), inner);
                    if (extracted == entry.input().amount())
                    {
                        inner.commit();
                        return true;
                    }
                    inner.abort();
                }
                return false;
            }
        }

        return false;
    }

    @Override
    public MeatRecipeSerialiser<?> getSerializer()
    {
        return NMrecipeTypes.BLOCK_CRUSHING_SERIALIZER;
    }

    @Override
    public MeatRecipeType<?> getType()
    {
        return NMrecipeTypes.GRINDING;
    }

    public boolean ejectOutputs(CrusherRecipeContext storage, TransactionContext transaction)
    {
        for (var view : storage.getInputStorage())
        {
            ItemVariant resource = view.getResource();
            BlockCrushingRegistry.Entry entry = getFromInput(resource);
            if (entry != null)
            {
                try (Transaction inner = transaction.openNested())
                {
                    boolean b1 = entry.output().insertInto(storage.getOutputStorage(), ItemVariant::of, inner);
                    boolean b2 = entry.extra().insertInto(storage.getOutputStorage(), ItemVariant::of, storage.getChanceMod(), inner);
                    if (b1 && b2)
                    {
                        inner.commit();
                        return true;
                    }
                    return false;
                }
            }
        }

        return false;
    }

    public long getBaseAmount()
    {
        return mainAmount;
    }

    public long getExtraAmount()
    {
        return extraAmount;
    }

    public float getChance()
    {
        return outputChance;
    }

    public static class Serialiser<T extends BlockCrushingRecipe> implements MeatRecipeSerialiser<T>
    {
        private final Constructor<T> constructor;
        private final Codec<RecipeFunction<T>> codec;

        public Serialiser(Constructor<T> constructor)
        {
            this.constructor = constructor;

            this.codec = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.LONG.fieldOf("base_amount").forGetter(MeatLibCodecs::notImplemented),
                    Codec.LONG.fieldOf("extra_amount").forGetter(MeatLibCodecs::notImplemented),
                    Codec.FLOAT.fieldOf("extra_chance").forGetter(MeatLibCodecs::notImplemented)
            ).apply(instance, (b, e, c) -> id -> constructor.create(id, b, e, c)));
        }

        @Override
        public Codec<RecipeFunction<T>> codec()
        {
            return codec;
        }

        @Override
        public T fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
        {
            return constructor.create(id, buf.readLong(), buf.readLong(), buf.readFloat());
        }

        @Override
        public void write(FriendlyByteBuf buf, BlockCrushingRecipe recipe)
        {
            buf.writeLong(recipe.mainAmount);
            buf.writeLong(recipe.extraAmount);
            buf.writeFloat(recipe.outputChance);
        }

        @FunctionalInterface
        public interface Constructor<T extends BlockCrushingRecipe>
        {
            T create(ResourceLocation id, long mainAmount, long extraAmount, float outputChance);
        }
    }
}
