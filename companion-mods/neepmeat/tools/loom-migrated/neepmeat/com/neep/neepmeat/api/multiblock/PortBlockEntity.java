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

package com.neep.neepmeat.api.multiblock;

import com.neep.neepmeat.block.entity.machine.VatControllerBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Collections;
import java.util.Iterator;

@SuppressWarnings("UnstableApiUsage")
public abstract class PortBlockEntity<T extends TransferVariant<?>> extends MultiBlock.Entity implements Storage<T>, IPortBlock.Entity
{
    private final Class<T> resourceClazz;

    public PortBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Class<T> clazz)
    {
        super(type, pos, state);
        this.resourceClazz = clazz;
    }

    @Override
    public long insert(T resource, long maxAmount, TransactionContext transaction)
    {
        if (hasController())
            return getController().getStorage(resourceClazz).insert(resource, maxAmount, transaction);

        return 0;
    }

    @Override
    public long extract(T resource, long maxAmount, TransactionContext transaction)
    {
        if (hasController())
            return getController().getStorage(resourceClazz).extract(resource, maxAmount, transaction);

        return 0;
    }

    @Override
    public Iterator<StorageView<T>> iterator()
    {
        VatControllerBlockEntity be;
        if ((be = (VatControllerBlockEntity) getController()) != null)
        {
            return be.getStorage(resourceClazz).iterator();
        }
        return Collections.emptyIterator();
    }
}
