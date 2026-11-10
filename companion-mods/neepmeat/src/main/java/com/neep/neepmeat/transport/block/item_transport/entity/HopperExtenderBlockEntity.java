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

package com.neep.neepmeat.transport.block.item_transport.entity;
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

import com.neep.neepmeat.api.storage.WritableStackStorage;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.block.item_transport.HopperExtenderBlock;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageUtil;
import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Iterator;

@SuppressWarnings("UnstableApiUsage")
public class HopperExtenderBlockEntity extends BlockEntity implements Storage<ItemVariant>
{

    private int transferCooldown = -1;
    private BlockApiCache<Storage<ItemVariant>, Direction> cache;

    protected WritableStackStorage storage;

    public HopperExtenderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        this.storage = new WritableStackStorage(null);
    }

    public HopperExtenderBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.HOPPER_EXTENDER, pos, state);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        storage.writeNbt(tag);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        storage.readNbt(tag);
    }

    public void updateApiCache(BlockPos pos, BlockState state)
    {
        if (getLevel() == null || !(getLevel() instanceof ServerLevel))
            return;

        Direction direction = state.getValue(HopperExtenderBlock.FACING);
        cache = BlockApiCache.create(ItemStorage.SIDED, (ServerLevel) getLevel(), pos.relative(direction));
    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, HopperExtenderBlockEntity blockEntity)
    {
        --blockEntity.transferCooldown;
        if (!blockEntity.needsCooldown())
        {
            blockEntity.setCooldown(8);
            insertTick(world, pos, state, blockEntity);
        }
    }

    private static void insertTick(Level world, BlockPos pos, BlockState state, HopperExtenderBlockEntity be)
    {
        if (be.getResource().isBlank())
            return;

        Direction targetDirection = state.getValue(HopperExtenderBlock.FACING).getOpposite();
        if (be.cache == null)
        {
            be.updateApiCache(pos, state);
        }

        Storage<ItemVariant> storage = be.cache.find(targetDirection);

        // Spawn item entities at open ends
        if (storage == null)
        {
            BlockPos offsetPos = pos.relative(targetDirection.getOpposite());
            BlockState offsetState = world.getBlockState(offsetPos);
            if (!offsetState.blocksMotion())
            {
                try (Transaction transaction = Transaction.openOuter())
                {
                    ItemEntity item = new ItemEntity(world,
                            offsetPos.getX() + 0.5,
                            offsetPos.getY() + (targetDirection.getAxis().isHorizontal() ? 0.1 : 0.5),
                            offsetPos.getZ() + 0.5,
                            be.getResource().toStack((int) be.getAmount()),
                            0, 0, 0);
                    be.extract(be.getResource(), be.getAmount(), transaction);
                    world.addFreshEntity(item);

                    transaction.commit();
                    return;
                }
            }
            return;
        }

        Transaction transaction = Transaction.openOuter();

        if (storage.supportsInsertion() && be.supportsExtraction() && !be.getResource().isBlank())
        {
            StorageUtil.move(be, storage, type -> true, 1, transaction);
        }

        transaction.commit();
    }

    private void setCooldown(int cooldown)
    {
        this.transferCooldown = cooldown;
    }

    private boolean needsCooldown()
    {
        return this.transferCooldown > 0;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction)
    {
        return storage.insert(resource, maxAmount, transaction);
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction)
    {
        return storage.extract(resource, maxAmount, transaction);
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator()
    {
        return storage.iterator();
    }

    public ItemVariant getResource()
    {
        return storage.getResource();
    }

    public long getAmount()
    {
        return storage.getAmount();
    }
}
