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

package com.neep.neepmeat.api.storage;

import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.function.Predicate;

@SuppressWarnings("UnstableApiUsage")
public class TypedFluidBuffer extends WritableSingleFluidStorage implements Storage<FluidVariant>
{
    protected Predicate<FluidVariant> validTypes;
    protected Mode mode;
    protected long capacity;
    protected BlockEntity parent;

    public TypedFluidBuffer(long capacity, Predicate<FluidVariant> validTypes, Mode mode, Runnable finalCallback)
    {
        super(capacity, finalCallback);
        this.validTypes = validTypes;
        this.mode = mode;
        this.capacity = capacity;
    }

    protected boolean canInsert(FluidVariant variant)
    {
        return validTypes.test(variant);
    }

    protected boolean canExtract(FluidVariant variant)
    {
        return true;
    }

    protected FluidVariant getBlankVariant()
    {
        return FluidVariant.BLANK;
    }

    protected long getCapacity(FluidVariant variant)
    {
        return capacity;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction)
    {
        if (!mode.canInsert())
            return 0;

        return insertDirect(resource, maxAmount, transaction);
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction)
    {
        if (!mode.canExtract())
            return 0;

        return extractDirect(resource, maxAmount, transaction);
    }

    public void setMode(Mode mode)
    {
        this.mode = mode;
    }

    public Mode getMode()
    {
        return mode;
    }

    public long insertDirect(FluidVariant resource, long maxAmount, TransactionContext transaction)
    {
        if (!validTypes.test(resource))
            return 0;

        if (getResource() == null || getResource().isBlank() || getAmount() <= 0)
        {
            this.amount = 0;
            this.variant = resource;
        }

        long inserted = Math.min(maxAmount, getCapacity() - getAmount());

        if (getResource().equals(resource) && inserted > 0)
        {
            this.updateSnapshots(transaction);
            amount += inserted;
            return inserted;
        }
        return 0;
    }

    public long extractDirect(FluidVariant resource, long maxAmount, TransactionContext transaction)
    {

        if (getResource() == null || getResource().isBlank() || getAmount() <= 0)
        {
            amount = 0;
            return 0;
        }

        long extracted = Math.min(maxAmount, getAmount());

        if (extracted > 0 && resource.equals(getResource()))
        {
            this.updateSnapshots(transaction);
            amount -= extracted;
            return extracted;
        }
        return 0;
    }

    public void clear()
    {
        this.amount = 0;
        this.variant = FluidVariant.BLANK;
    }

    public boolean supportsInsertion()
    {
        return mode.canInsert();
    }

    public boolean supportsExtraction()
    {
        return mode.canExtract();
    }

    public enum Mode
    {
        INSERT_EXTRACT,
        INSERT_ONLY,
        EXTRACT_ONLY;

        boolean canInsert()
        {
            return this == INSERT_EXTRACT || this == INSERT_ONLY;
        }

        boolean canExtract()
        {
            return this == INSERT_EXTRACT || this == EXTRACT_ONLY;
        }
    }
}
