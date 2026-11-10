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

package com.neep.neepmeat.machine.advanced_integrator;

import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.api.DataPort;
import com.neep.neepmeat.api.DataResource;
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class SimpleDataPort implements DataPort, NbtSerialisable
{
    @Nullable private LazyBlockApiCache<DataPort, Void> target;

    private final BlockEntity parent;

    public SimpleDataPort(BlockEntity parent)
    {
        this.parent = parent;
    }

    @Override
    public void setTarget(BlockPos target)
    {
        this.target = LazyBlockApiCache.of(DataPort.DATA_PORT, target, parent::getLevel, () -> null);
    }

    @Override
    public long receive(DataResource variant, long amount, TransactionContext transaction)
    {
        return 0;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        if (target != null)
        {
            nbt.put("target", NbtUtils.writeBlockPos(target.pos()));
        }
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        if (nbt.contains("target"))
        {
            this.target = LazyBlockApiCache.of(DataPort.DATA_PORT,
                    NbtUtils.readBlockPos(nbt.getCompound("target")),
                    parent::getLevel,
                    () -> null);
        }
    }

    public long send(DataResource variant, long amount , TransactionContext transaction)
    {
        if (target != null)
        {
            var found = target.find();
            if (found != null)
            {
                return found.receive(variant, amount, transaction);
            }
        }
        return 0;
    }
}
