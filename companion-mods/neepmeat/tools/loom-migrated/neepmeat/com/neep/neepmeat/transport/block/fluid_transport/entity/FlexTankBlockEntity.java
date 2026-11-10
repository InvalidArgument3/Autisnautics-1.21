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

package com.neep.neepmeat.transport.block.fluid_transport.entity;

import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.transport.block.FlexStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;

public class FlexTankBlockEntity extends FlexStorageBlockEntity<FlexTankBlockEntity>
{
    private final StorageCache<FluidVariant> cache = new StorageCache<>(FluidStorage.SIDED, this);
    private final FlexTankStorage storage;

    private final long capacity;

    public FlexTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, long capacity)
    {
        super(type, pos, state);
        this.capacity = capacity;
        this.storage = new FlexTankStorage(this::setChanged);
    }

    public Storage<FluidVariant> getStorage(Direction ignored)
    {
        return storage;
    }

    @Override
    protected StorageDelegate<FlexTankBlockEntity> getStorageDelegate()
    {
        return storage;
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        storage.writeNbt(nbt);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        storage.readNbt(nbt);
    }

    class FlexTankStorage extends WritableSingleFluidStorage implements StorageDelegate<FlexTankBlockEntity>
    {
        public FlexTankStorage(Runnable finalCallback)
        {
            super(0, finalCallback);
        }

        @Override
        public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction)
        {
            if (isRoot())
            {
                return super.insert(insertedVariant, maxAmount, transaction);
            }
            else if (root != null)
            {
                return cache.find().insert(insertedVariant, maxAmount, transaction);
            }
            return 0;
        }

        @Override
        public long extract(FluidVariant extractedVariant, long maxAmount, TransactionContext transaction)
        {
            if (isRoot())
            {
                return super.extract(extractedVariant, maxAmount, transaction);
            }
            else if (root != null)
            {
                return cache.find().extract(extractedVariant, maxAmount, transaction);
            }
            return 0;
        }

        @Override
        public @NotNull Iterator<StorageView<FluidVariant>> iterator()
        {
            if (isRoot())
            {
                return super.iterator();
            }
            else if (root != null)
            {
                return cache.find().iterator();
            }
            return Collections.emptyIterator();
        }

        @Override
        protected long getCapacity(FluidVariant variant)
        {
            return children.size() * FlexTankBlockEntity.this.capacity;
        }

        @Override
        public void validate()
        {
            amount = Math.min(getCapacity(), amount);
        }

        @Override
        public boolean isEmpty()
        {
            return amount == 0;
        }

        @Override
        public void transferInto(FlexTankBlockEntity root, TransactionContext transaction)
        {
            FluidVariant variant = getResource();
            long extracted = extract(variant, getAmount(), transaction);

            if (extracted > 0)
            {
                // Since this is a fluid container, there's no other option than voiding the excess
                long forwarded = root.storage.insert(variant, extracted, transaction);
            }
        }
    }
}
