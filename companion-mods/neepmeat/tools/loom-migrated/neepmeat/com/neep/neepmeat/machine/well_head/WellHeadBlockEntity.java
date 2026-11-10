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

package com.neep.neepmeat.machine.well_head;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.meatlib.util.LazySupplier;
import com.neep.neepmeat.api.FluidPump;
import com.neep.neepmeat.api.processing.random_ores.RandomOreProvider;
import com.neep.neepmeat.api.processing.random_ores.RandomOres;
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import com.neep.neepmeat.transport.fluid_network.node.AcceptorModes;
import com.neep.neepmeat.transport.util.ItemPipeUtil;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class WellHeadBlockEntity extends SyncableBlockEntity
{
//    private final WritableSingleFluidStorage fluidStorage = new WritableSingleFluidStorage(8 * FluidConstants.BUCKET, this::markDirty);
    private final LazyBlockApiCache<Storage<FluidVariant>, Direction> outputCache;
    private final FluidPump pump = FluidPump.of(-0.5f, () -> AcceptorModes.PUSH);
    private final LazySupplier<RandomOreProvider> oreProvider = LazySupplier.of(() ->
            RandomOres.INSTANCE.makeProvider(getLevel(), getBlockPos(), RandomSource.create()));

    public WellHeadBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        outputCache = LazyBlockApiCache.of(FluidStorage.SIDED, pos.relative(state.getValue(WellHeadBlock.FACING)), this::getLevel, () -> state.getValue(WellHeadBlock.FACING));
    }

    public void serverTick()
    {
    }

    public void receiveFluid(long distributeAmount, TransactionContext transaction)
    {
        if (level instanceof ServerLevel serverWorld)
        {
            Direction facing = getBlockState().getValue(WellHeadBlock.FACING);
            for (int i = 0; i < 2; ++i)
            {
                for (ItemStack stack : oreProvider.get().random(serverWorld, worldPosition))
                {
                    long ejected = ItemPipeUtil.EjectOperation.of(serverWorld, worldPosition, facing)
                            .useRouting(true)
                            .stackToAny(ItemVariant.of(stack), stack.getCount(), transaction)
                            .amount();

                    if (ejected < stack.getCount())
                    {
                        MeatlibStorageUtil.scatterAmount(level, worldPosition, ItemVariant.of(stack), stack.getCount());
                    }
                }
            }
        }
    }

    public FluidPump getFluidPump(Direction face)
    {
        if (face == getBlockState().getValue(WellHeadBlock.FACING))
        {
            return pump;
        }
        return null;
    }

    public Storage<FluidVariant> getFluidStorage(Direction face)
    {
        if (face == getBlockState().getValue(WellHeadBlock.FACING))
        {
//            return fluidStorage;
        }
        return null;
    }
}
