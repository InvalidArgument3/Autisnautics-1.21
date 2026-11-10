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

package com.neep.neepmeat.transport.block.item_transport.machine;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.storage.DynamicLazyBlockApiCache;
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ItemInterfaceBlockEntity extends SyncableBlockEntity
{
    protected final LazyBlockApiCache<Storage<ItemVariant>, Direction> cache = addListener(DynamicLazyBlockApiCache.facing(
            ItemStorage.SIDED, this, () -> getBlockState().getValue(ItemInterfaceBlock.FACING)));

    public ItemInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public Storage<ItemVariant> getStorage(@Nullable Direction direction)
    {
        // Weirdness might happen if the target block attempts to access its own interface
        if (direction == getBlockState().getValue(ItemInterfaceBlock.FACING))
            return null;

        // Naughty player!
        if (cache.getBlockEntity() instanceof ItemInterfaceBlockEntity)
            return null;

        return cache.find();
    }
}
