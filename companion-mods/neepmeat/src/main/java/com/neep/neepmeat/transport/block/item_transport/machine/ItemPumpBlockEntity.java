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

import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.meatlib.storage.ResourcePredicate;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.neepmeat.transport.interfaces.IServerWorld;
import com.neep.neepmeat.transport.item_network.RetrievalTarget;
import com.neep.neepmeat.transport.item_network.ShittyRoutingListener;
import com.neep.neepmeat.transport.util.ItemPipeUtil;
import com.neep.meatlib.transfer.ItemStorage;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageUtil;
import com.neep.meatlib.transfer.ResourceAmount;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ItemPumpBlockEntity extends EjectorBlockEntity implements ShittyRoutingListener
{
    protected List<RetrievalTarget<ItemVariant>> retrievalCache = null;

    public ItemPumpBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public ItemPumpBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.ITEM_PUMP, pos, state);
    }

    @Override
    public void serverTick()
    {
        if (retrievalCache == null)
        {
            Direction face = getBlockState().getValue(ItemPumpBlock.FACING).getOpposite();
            updateRetrievalCache((ServerLevel) level, worldPosition, face, this);
        }

        super.serverTick();
    }

    @Override
    protected void tryTransfer()
    {
        BlockState state = getBlockState();
        Direction facing = state.getValue(BaseFacingBlock.FACING);

        // Try to extract from adjacent storage
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, worldPosition.relative(facing.getOpposite()), facing);
        if (storage != null)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                Storage<ItemVariant> facingStorage = insertionCache.find();

                long canInsert = 16;
                ResourcePredicate<ItemVariant> predicate = (v, a) -> a;
                if (facingStorage != null)
                    predicate =  (v, a) -> MeatlibStorageUtil.simulateInsert(facingStorage, v, a, transaction);

                ResourceAmount<ItemVariant> extracted = MeatlibStorageUtil.findAndExtractContent(storage, canInsert, transaction, predicate);

                if (extracted == null || extracted.amount() <= 0)
                {
                    transaction.abort();
                    return;
                }

                //                if (facingStorage != null)
//                {
//                    canInsert = Math.min(canInsert, MeatlibStorageUtil.simulateInsert(facingStorage, extracted.resource(), canInsert, transaction));
//                }

                stored = new ResourceAmount<>(extracted.resource(), extracted.amount());

                succeed();
                transaction.commit();
            }
        }

        // Try to retrieve from pipes
        else if (level.getBlockState(worldPosition.relative(facing.getOpposite())).getBlock() instanceof ItemPipe pipe)
        {
            Transaction transaction = Transaction.openOuter();
            if (retrieve(transaction))
            {
                succeed();
                transaction.commit();
            }
            else
            {
                transaction.abort();
            }
        }
    }

    // Takes items from connected storages
    public boolean retrieve(Transaction transaction)
    {
        if (retrievalCache == null)
            return false;

        boolean success = false;

        Storage<ItemVariant> facingStorage = insertionCache.find();

        for (RetrievalTarget<ItemVariant> target : retrievalCache)
        {
            Storage<ItemVariant> targetStorage = target.find();

            Transaction nested1 = transaction.openNested();

            ResourceAmount<ItemVariant> extractable;
            if (facingStorage != null)
            {
                Transaction nested2 = nested1.openNested();
                extractable = MeatlibStorageUtil.findExtractableContent(targetStorage,
                        (t, itemVariant) -> MeatlibStorageUtil.simulateInsert(facingStorage, itemVariant, Long.MAX_VALUE, t) > 0, nested2);
                nested2.abort();
            }
            else
            {
                extractable = StorageUtil.findExtractableContent(targetStorage, nested1);
            }

            if (extractable == null)
            {
                nested1.abort();
                continue;
            }

            long transferable = canForward(extractable, nested1);
            if (transferable < 1)
            {
                nested1.abort();
                continue;
            }

            // TODO: change max amount
            long extracted = targetStorage.extract(extractable.resource(), Math.min(transferable, 16), nested1);
            extractable = new ResourceAmount<>(extractable.resource(), extracted);
            long forwarded = forwardRetrieval(new ResourceAmount<>(extractable.resource(), extracted), target, nested1);

            if (forwarded < 1)
            {
                nested1.abort();
                continue;
            }
            nested1.commit();
            success = true;
        }
        return success;
    }

    public long forwardRetrieval(ResourceAmount<ItemVariant> amount, RetrievalTarget<ItemVariant> target, TransactionContext transaction)
    {
        Direction facing = getBlockState().getValue(ItemPumpBlock.FACING);
        return ((IServerWorld) level).getItemNetwork().route(target.getPos(), target.accessFace(), worldPosition.relative(facing.getOpposite()), facing, amount.resource(), (int) amount.amount(), transaction);
    }

    private void updateRetrievalCache(ServerLevel world, BlockPos pos, Direction face, ItemPumpBlockEntity be)
    {
        retrievalCache = ItemPipeUtil.floodSearch(this, pos, face, world, 16);
    }

    public long canForward(ResourceAmount<ItemVariant> amount, Transaction transaction)
    {
        Storage<ItemVariant> storage;
        if (insertionCache != null && (storage = insertionCache.find()) != null)
        {
            return MeatlibStorageUtil.simulateInsert(storage, amount.resource(), amount.amount(), transaction);
        }
        return amount.amount();
    }

    @Override
    public void invalidate()
    {
        retrievalCache = null;
    }
}
