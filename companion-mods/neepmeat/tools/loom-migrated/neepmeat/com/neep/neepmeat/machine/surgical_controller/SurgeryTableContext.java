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

package com.neep.neepmeat.machine.surgical_controller;

import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import com.neep.neepmeat.plc.component.TableComponent;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A mutable view of the surgery table's constituent storages
 */
@SuppressWarnings("UnstableApiUsage")
public class SurgeryTableContext implements NbtSerialisable
{
    private final List<BlockApiCache<TableComponent<?>, Void>> caches = new ArrayList<>(9);
    private final List<BlockPos> posList = new ArrayList<>(9);

    public boolean isAssembled;

    WritableStackStorage storage = new WritableStackStorage(() -> {}, Integer.MAX_VALUE)
    {
        @Override
        public boolean supportsInsertion()
        {
            return false;
        }
    };

    @Nullable
    public TableComponent<TransferVariant<?>> getStructure(int i)
    {
        // TODO: This is a problem
//        return (TableComponent<? extends TransferVariant<?>>) TableComponent.STRUCTURE_LOOKUP.find(world, posList.get(i), null);
        return (TableComponent<TransferVariant<?>>) caches.get(i).find(null);
    }

    public int getWidth()
    {
        return 3;
    }

    public int getHeight()
    {
        return 3;
    }

    public int getSize()
    {
        return posList.size();
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        storage.writeNbt(nbt);

        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        storage.readNbt(nbt);
    }

    public void add(ServerLevel world, BlockPos pos)
    {
        posList.add(pos.immutable());
        caches.add(BlockApiCache.create(TableComponent.LOOKUP, world, pos.immutable()));
    }

    public BlockPos getPos(int i)
    {
        return posList.get(i);
    }

    public void clear()
    {
        caches.clear();
        posList.clear();
    }

    public Storage<ItemVariant> getStorage()
    {
        return storage;
    }
}
