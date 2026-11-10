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

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import java.util.function.BiFunction;

public interface RecipeOutput<T>
{
    T resource();

    /**
     * Generates a random output amount. The result is random, so call this once and store the value for further use.
     * Do expect the returned amount to be the same amount used in RecipeOutput::insertInto.
     */
    long randomAmount(float chanceMod);
    long maxAmount();
    long minAmount();
    float chance();
//    void update();
    CompoundTag getNbt();

    IngredientType<T> getType();

    default <V extends TransferVariant<T>> boolean insertInto(Storage<V> storage, BiFunction<T, CompoundTag, V> of, TransactionContext transaction)
    {
        return insertInto(storage, of, 0, transaction);
    }

    <V extends TransferVariant<T>> boolean insertInto(Storage<V> storage, BiFunction<T, CompoundTag, V> of, float chanceModifier, TransactionContext transaction);

    void write(FriendlyByteBuf buf);

    RecipeOutput<Object> EMPTY = new RecipeOutput<>()
    {
        @Override
        public Object resource()
        {
            return null;
        }

        @Override
        public long randomAmount(float chanceMod)
        {
            return 0;
        }

        @Override
        public long maxAmount()
        {
            return 0;
        }

        @Override
        public long minAmount()
        {
            return 0;
        }

        @Override
        public float chance()
        {
            return 0;
        }

        @Override
        public CompoundTag getNbt()
        {
            return null;
        }

        @Override
        public IngredientType<Object> getType()
        {
            return Ingredients.EMPTY;
        }

        @Override
        public <V extends TransferVariant<Object>> boolean insertInto(Storage<V> storage, BiFunction<Object, CompoundTag, V> of, float chanceMod, TransactionContext transaction)
        {
            return true;
        }

        @Override
        public void write(FriendlyByteBuf buf)
        {

        }
    };

    static <T> RecipeOutput<T> empty()
    {
        return (RecipeOutput<T>) EMPTY;
    }
}
