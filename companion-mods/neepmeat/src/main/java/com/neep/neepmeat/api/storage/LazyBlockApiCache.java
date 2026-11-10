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

import com.neep.meatlib.blockentity.BlockEntityInvalidateListener;
import com.neep.neepmeat.plc.instruction.Argument;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import com.neep.meatlib.transfer.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Conveniently hides the logic of properly handling a BlockApiCache in a BlockEntity, where a ServerWorld is not consistently available.
 * @param <A> API type
 * @param <C> Context type
 */
public interface LazyBlockApiCache<A, C> extends BlockEntityInvalidateListener
{
    @Nullable A find();

    @Nullable BlockEntity getBlockEntity();

    void invalidate();

    BlockPos pos();

    C ctx();

    static <A, C> LazyBlockApiCache<A, C> of(BlockApiLookup<A, C> lookup, BlockPos pos, Supplier<Level> world, Supplier<C> ctxSupplier)
    {
        return new LazyBlockApiCacheImpl<>(world, lookup, pos, ctxSupplier);
    }

    static <A> LazyBlockApiCache<A, Direction> of(BlockApiLookup<A, Direction> lookup, Supplier<Level> world, Argument argument)
    {
        return new LazyBlockApiCacheImpl<>(world, lookup, argument.pos(), argument::face);
    }

    static LazyBlockApiCache<Storage<ItemVariant>, Direction> itemSided(Argument argument, Supplier<Level> world)
    {
        return new LazyBlockApiCacheImpl<>(world, ItemStorage.SIDED, argument.pos(), argument::face);
    }

    static LazyBlockApiCache<Storage<FluidVariant>, Direction> fluidSided(Argument argument, Supplier<Level> world)
    {
        return new LazyBlockApiCacheImpl<>(world, FluidStorage.SIDED, argument.pos(), argument::face);
    }
}
