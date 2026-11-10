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

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

// TODO: Refactor into interface and impl, add null object implementation
public class LazyBlockApiCacheImpl<A, C> implements LazyBlockApiCache<A, C>
{
    private BlockApiCache<A, C> cache = null;
    private final Supplier<ServerLevel> worldSupplier;
    private final BlockApiLookup<A, C> lookup;
    private final BlockPos pos;
    private final Supplier<C> ctxSupplier;

    LazyBlockApiCacheImpl(Supplier<Level> worldSupplier, BlockApiLookup<A, C> lookup, BlockPos pos, Supplier<C> ctxSupplier)
    {
        this.worldSupplier = () ->
        {
            if (worldSupplier.get() instanceof ServerLevel serverWorld)
            {
                return serverWorld;
            }
            throw new IllegalArgumentException("LazyBlockApiCache queried on the client!");
        };
        this.lookup = lookup;
        this.pos = pos;
        this.ctxSupplier = ctxSupplier;
    }

    @Override
    public A find()
    {
        validate();
        return cache.find(ctxSupplier.get());
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity()
    {
        validate();
        return cache.getBlockEntity();
    }

    @Override
    public void invalidate()
    {
        cache = null;
    }

    private void validate()
    {
        if (cache == null)
        {
            cache = BlockApiCache.create(lookup, worldSupplier.get(), pos);
        }
    }

    @Override
    public BlockPos pos()
    {
        return pos;
    }

    @Override
    public C ctx()
    {
        return ctxSupplier.get();
    }

}
