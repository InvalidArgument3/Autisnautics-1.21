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

package com.neep.neepmeat.transport.block.fluid_transport.machine;
import com.neep.neepmeat.machine.transport.ConnectedPipeBlock;
import com.neep.neepmeat.machine.transport.DeferredRequest;
import com.neep.neepmeat.machine.transport.FilterEntry;
import com.neep.neepmeat.machine.transport.FilterPipeMenu;
import com.neep.neepmeat.machine.transport.NetworkCrafterBlockEntity;
import com.neep.neepmeat.machine.transport.StorageBusBlock;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.block.fluid_transport.FluidPipeBlock;
import com.neep.neepmeat.transport.block.fluid_transport.PumpBlock;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidPipeBlockEntity;
import com.neep.neepmeat.transport.block.fluid_transport.machine.PumpBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.FilterItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.ItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.MergePipeBlock;
import com.neep.neepmeat.transport.block.item_transport.PipeDriverBlock;
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity;
import com.neep.neepmeat.transport.item_network.PipeRouteFinder;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.storage.DynamicLazyBlockApiCache;
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.block.fluid_transport.FluidInterfaceBlock;
import com.neep.meatlib.transfer.FluidStorage;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class FluidInterfaceBlockEntity extends SyncableBlockEntity
{
    protected final LazyBlockApiCache<Storage<FluidVariant>, Direction> cache =
            addListener(DynamicLazyBlockApiCache.facing(FluidStorage.SIDED, this, () -> getBlockState().getValue(FluidInterfaceBlock.FACING)));

    public FluidInterfaceBlockEntity(BlockEntityType type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public FluidInterfaceBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.FLUID_INTERFACE, pos, state);
    }

//    public void updateCache()
//    {
//        Direction facing = getCachedState().get(FluidInterfaceBlock.FACING);
//        BlockPos offset = pos.offset(facing);
//        this.cache = LazyBlockApiCache.of(FluidStorage.SIDED, offset, this::getWorld, facing::getOpposite);
//    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
    }

//    @Override
//    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction)
//    {
//        Storage<FluidVariant> storage = cache.find();
//        if (storage != null)
//        {
//            return storage.insert(resource, maxAmount, transaction);
//        }
//        return 0;
//    }
//
//    @Override
//    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction)
//    {
//        Storage<FluidVariant> storage = cache.find();
//        if (storage != null)
//        {
//            return storage.extract(resource, maxAmount, transaction);
//        }
//        return 0;
//    }
//
//    @Override
//    public Iterator<StorageView<FluidVariant>> iterator()
//    {
//        Storage<FluidVariant> storage = cache.find();
//        if (storage != null)
//        {
//            return storage.iterator();
//        }
//        return Collections.emptyIterator();
//    }

    public Storage<FluidVariant> getStorage(@Nullable Direction direction)
    {
        // Weirdness might happen if the target block attempts to access its own interface
        if (direction == getBlockState().getValue(FluidInterfaceBlock.FACING))
            return null;

        // Naughty player!
        if (cache.getBlockEntity() instanceof FluidInterfaceBlockEntity)
            return null;

        return cache.find();
    }
}
