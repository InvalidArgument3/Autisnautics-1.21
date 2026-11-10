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

import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.TransferVariant;
import com.neep.meatlib.transfer.TransactionContext;

public record MatchingStorageView<T extends TransferVariant<?>>(T variant, long amount) implements StorageView<T>
{
    public long extract(T resource, long maxAmount, TransactionContext transaction)
    {
        return 0;
    }

    @Override
    public boolean isResourceBlank()
    {
        return variant.isBlank();
    }

    @Override
    public T getResource()
    {
        return variant;
    }

    @Override
    public long getAmount()
    {
        return amount;
    }

    @Override
    public long getCapacity()
    {
        return 0;
    }
}
