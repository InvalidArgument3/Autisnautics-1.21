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
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class DynamicLazyBlockApiCache<A, C> implements LazyBlockApiCache<A, C>
{
    @Nullable private BlockApiCache<A, C> cache = null;

    private final BlockApiLookup<A, C> lookup;

    private final Supplier<ServerLevel> worldSupplier;
    protected final Supplier<BlockPos> posSupplier;
    protected final Supplier<C> ctxSupplier;

    public static <A, C> LazyBlockApiCache<A, C> of(Supplier<Level> worldSupplier, BlockApiLookup<A, C> lookup, Supplier<BlockPos> posSupplier, Supplier<C> ctxSupplier)
    {
        return new DynamicLazyBlockApiCache<>(worldSupplier, lookup, posSupplier, ctxSupplier);
    }

    public static <A> LazyBlockApiCache<A, Direction> facing(BlockApiLookup<A, Direction> lookup, BlockEntity be, EnumProperty<Direction> property)
    {
        return of(be::getLevel, lookup,
                () -> be.getBlockPos().relative(be.getBlockState().getValue(property)),
                () -> be.getBlockState().getValue(property));
    }

    public static <A> LazyBlockApiCache<A, Direction> facing(BlockApiLookup<A, Direction> lookup, BlockEntity be, Supplier<Direction> ctxSupplier)
    {
        return of(be::getLevel, lookup,
                () -> be.getBlockPos().relative(ctxSupplier.get()),
                () -> ctxSupplier.get().getOpposite());
    }

    public static LazyBlockApiCache<Void, Void> voidCtx(BlockApiLookup<Void, Void> lookup, Supplier<Level> worldSupplier, Supplier<BlockPos> posSupplier)
    {
        return of(worldSupplier, lookup,
                posSupplier,
                () -> null);
    }

    protected DynamicLazyBlockApiCache(Supplier<Level> worldSupplier, BlockApiLookup<A, C> lookup, Supplier<BlockPos> posSupplier, Supplier<C> ctxSupplier)
    {
        this.worldSupplier = () ->
        {
            if (worldSupplier.get() instanceof ServerLevel serverWorld)
            {
                return serverWorld;
            }
            return null;
        };
        this.lookup = lookup;
        this.posSupplier = posSupplier;
        this.ctxSupplier = ctxSupplier;
    }

    @Override
    public A find()
    {
        validate();
        if (cache != null)
            return cache.find(ctxSupplier.get());

        return null;
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity()
    {
        validate();
        return cache != null ? cache.getBlockEntity() : null;
    }

    @Override
    public void invalidate()
    {
        cache = null;
    }

    protected void validate()
    {
        if (cache == null)
        {
            ServerLevel world = worldSupplier.get();
            if (world != null)
                cache = BlockApiCache.create(lookup, world, posSupplier.get());
        }
    }

    @Override
    public BlockPos pos()
    {
        return posSupplier.get();
    }

    @Override
    public C ctx()
    {
        return ctxSupplier.get();
    }

}
