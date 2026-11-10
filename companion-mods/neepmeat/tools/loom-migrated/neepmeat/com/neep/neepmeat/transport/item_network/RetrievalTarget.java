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

package com.neep.neepmeat.transport.item_network;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public record RetrievalTarget<T extends TransferVariant<?>>(BlockApiCache<Storage<T>, Direction> cache, Direction accessFace)
{
    public static <T extends TransferVariant<?>> RetrievalTarget<T> of(BlockApiCache<Storage<T>, Direction> cache, Direction accessFace)
    {
        return new RetrievalTarget<>(cache, accessFace);
    }

    public static <T extends TransferVariant<?>> RetrievalTarget<T> of(BlockApiLookup<Storage<T>, Direction> lookup, ServerLevel world, BlockPos pos, Direction accessFace)
    {
        return new RetrievalTarget<>(BlockApiCache.create(lookup, world, pos), accessFace);
    }

    public BlockPos getPos()
    {
        return cache.getPos();
    }

    @Nullable public Storage<T> find()
    {
        return cache.find(accessFace);
    }
}
