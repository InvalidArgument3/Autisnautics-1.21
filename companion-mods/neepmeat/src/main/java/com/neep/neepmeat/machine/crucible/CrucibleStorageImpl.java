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

package com.neep.neepmeat.machine.crucible;

import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.SingleSlotStorage;
import com.neep.meatlib.transfer.SingleVariantStorage;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class CrucibleStorageImpl implements CrucibleStorage, NbtSerialisable
{
    protected CrucibleBlockEntity parent;
    protected WritableSingleFluidStorage fluidStorage;

    protected SingleVariantStorage<ItemVariant> itemStorage;

    public CrucibleStorageImpl(CrucibleBlockEntity parent)
    {
        this.parent = parent;
        this.fluidStorage = new WritableSingleFluidStorage(FluidConstants.BUCKET, parent::sync);

        this.itemStorage = new WritableStackStorage(parent::sync, 16)
        {
            @Override
            public long insert(ItemVariant insertedVariant, long maxAmount, TransactionContext transaction)
            {
                // Sometimes empty item stack entities can appear
                if (insertedVariant.isBlank() || maxAmount == 0)
                    return 0;

                long processed = 0;
                try (Transaction inner = transaction.openNested())
                {
                    long inserted = super.insert(insertedVariant, maxAmount, inner);
                    if (inserted > 0)
                    {
                        processed = parent.processItem(inserted, inner);
                    }

                    if (processed > 0)
                        inner.commit();
                    else
                        inner.abort();
                }
                return processed;
            }

            protected ItemVariant getBlankVariant()
            {
                return ItemVariant.BLANK;
            }
        };
    }

    @Override
    public SingleSlotStorage<FluidVariant> getStorage(@Nullable Direction direction)
    {
        return fluidStorage;
    }

    @Override
    public SingleSlotStorage<ItemVariant> getItemStorage(@Nullable Direction direction)
    {
        return direction == Direction.UP || direction == null ? itemStorage : null;
    }

    public Storage<FluidVariant> getFluidOutput()
    {
        return parent.getOutput();
    }

    @Override
    public float discount()
    {
        return 0;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        fluidStorage.toNbt(nbt);
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        fluidStorage.readNbt(nbt);
    }
}
