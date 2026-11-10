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

package com.neep.neepmeat.transport.api;

import com.neep.meatlib.blockentity.BlockEntityInvalidateListener;
import com.neep.neepmeat.api.FluidPump;
import com.neep.neepmeat.api.storage.DynamicLazyBlockApiCache;
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface AutoEjector<T> extends BlockEntityInvalidateListener
{
    static SingleFaceFluid<FluidVariant> simpleFluid(Supplier<Direction> side, BlockEntity blockEntity, Storage<FluidVariant> backing, FluidPump pump)
    {
        return new SingleFaceFluid<>(side, blockEntity::getLevel, blockEntity.getBlockPos(), backing, FluidStorage.SIDED, pump);
    }

    static SingleFaceFluid<FluidVariant> simpleFluid(Supplier<Direction> side, Supplier<Level> worldSupplier, BlockPos pos, Storage<FluidVariant> backing, FluidPump pump)
    {
        return new SingleFaceFluid<>(side, worldSupplier, pos, backing, FluidStorage.SIDED, pump);
    }

    void tick(@Nullable Transaction transaction);

    void invalidate();

    class SingleFaceFluid<T> implements AutoEjector<T>
    {
        private final Supplier<Level> worldSupplier;
        private final Storage<T> backing;
        private final FluidPump pump;
        private final LazyBlockApiCache<Storage<T>, Direction> apiCache;

        private SingleFaceFluid(Supplier<Direction> side, Supplier<Level> worldSupplier, BlockPos pos, Storage<T> backing, BlockApiLookup<Storage<T>, Direction> lookup, FluidPump pump)
        {
            this.worldSupplier = worldSupplier;
            this.backing = backing;
            this.pump = pump;
//            this.apiCache = LazyBlockApiCache.of(lookup, pos.offset(side), worldSupplier, side::getOpposite);
            this.apiCache = DynamicLazyBlockApiCache.of(worldSupplier, lookup, () -> pos.relative(side.get()), () -> side.get().getOpposite());
        }

        @Override
        public void tick(@Nullable Transaction transaction)
        {
            if (worldSupplier.get() instanceof ServerLevel serverWorld && serverWorld.getGameTime() % 2 == 0)
            {
                boolean fluidPresent = true;

                // Minor optimisation for single variant storages
                if (backing instanceof SingleVariantStorage<?> single)
                {
                    fluidPresent = single.getAmount() > 0;
                }

                if (fluidPresent)
                {
                    Storage<T> storage = apiCache.find();
                    if (storage != null)
                    {
                        int rate = (int) (2 * this.pump.getFlow() * FluidTransportConstants.INSTANCE.maxTransfer());
                        if (rate < 0) // Negative rate for push
                        {
                            StorageUtil.move(backing, storage, v -> true, -rate, transaction);
                        }
                    }
                }
            }
        }

        @Override
        public void invalidate()
        {
            apiCache.invalidate();
        }
    }
//    class Impl<T> implements AutoEjector<T>
//    {
//        private final Set<Direction> sides;
//        private final Storage<T> backing;
//
//        public Impl(Set<Direction> sides, Storage<T> backing)
//        {
//            this.sides = sides;
//            this.backing = backing;
//        }
//
//        @Override
//        public void tick()
//        {
//
//        }
//    }
}
