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

package com.neep.neepmeat.item.util;

import com.google.common.collect.Iterators;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class LivingItemStorage extends SnapshotParticipant<Integer> implements InsertionOnlyStorage<FluidVariant>, StorageView<FluidVariant>
{
    protected final ItemStack stack;
    protected final ContainerItemContext context;

    public LivingItemStorage(ItemStack stack, ContainerItemContext context)
    {
        this.stack = stack.copy();
        this.context = context;
    }

    public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction)
    {
        int fluidPerDurability = LivingTools.getFluidForDurability(insertedVariant);
        if (fluidPerDurability != -1)
        {
            long durabilityIncrease = Math.floorDiv(maxAmount, fluidPerDurability);
            durabilityIncrease = Math.min(durabilityIncrease, stack.getDamageValue());
            long amountConsumed = Math.min(durabilityIncrease * fluidPerDurability, maxAmount);
            if (durabilityIncrease > 0)
            {
                updateSnapshots(transaction);
                stack.setDamageValue((int) (stack.getDamageValue() - durabilityIncrease));
                context.exchange(ItemVariant.of(stack), 1, transaction);
                return amountConsumed;
            }
        }
        return 0;
    }

    @Override
    public boolean isResourceBlank()
    {
        return true;
    }

    @Override
    public FluidVariant getResource()
    {
        return FluidVariant.BLANK;
    }

    @Override
    public long getAmount()
    {
        return 0;
    }

    @Override
    public long getCapacity()
    {
        return 0;
    }

    public @NotNull Iterator<StorageView<FluidVariant>> iterator()
    {
        return Iterators.singletonIterator(this);
    }

    protected Integer createSnapshot()
    {
        return stack.getDamageValue();
    }

    protected void readSnapshot(Integer snapshot)
    {
        stack.setDamageValue(snapshot);
//        context.exchange(ItemVariant.of(stack), stack.getCount(),
    }

    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction)
    {
        return 0;
    }
}
