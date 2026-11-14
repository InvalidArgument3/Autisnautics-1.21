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

package com.neep.meatlib.transfer;

import com.neep.meatlib.transfer.SingleSlotStorage;
import com.neep.meatlib.transfer.TransactionContext;
import com.neep.meatlib.transfer.SnapshotParticipant;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class SingleEntityStorage extends SnapshotParticipant<EntityVariant<?>> implements SingleSlotStorage<EntityVariant<?>>
{
    @NotNull  public EntityVariant<?> variant = EntityVariant.getBlank();

    public static SingleEntityStorage of(Entity entity)
    {
        SingleEntityStorage storage = new SingleEntityStorage();
        storage.variant = entity != null ? EntityVariant.of(entity) : EntityVariant.getBlank();
        return storage;
    }

    @Override
    public long insert(EntityVariant<?> resource, long maxAmount, TransactionContext transaction)
    {
        if (!isResourceBlank() || maxAmount == 0) return 0;

        maxAmount = Math.min(getCapacity(), maxAmount);

        return 0;
    }

    @Override
    public long extract(EntityVariant<?> resource, long maxAmount, TransactionContext transaction)
    {
        return 0;
    }

    @Override
    public boolean isResourceBlank()
    {
        return Objects.equals(getResource(), EntityVariant.getBlank());
    }

    @Override
    public EntityVariant<?> getResource()
    {
        return variant;
    }

    @Override
    public long getAmount()
    {
        return isResourceBlank() ? 0 : 1;
    }

    @Override
    public long getCapacity()
    {
        return 1;
    }

    @Override
    protected EntityVariant<?> createSnapshot()
    {
        return getResource();
    }

    @Override
    protected void readSnapshot(EntityVariant<?> snapshot)
    {
        this.variant = snapshot;
    }

    public interface EntityInterface
    {
        void setEntity(Entity entity);

        Entity getEntity();
    }
}
