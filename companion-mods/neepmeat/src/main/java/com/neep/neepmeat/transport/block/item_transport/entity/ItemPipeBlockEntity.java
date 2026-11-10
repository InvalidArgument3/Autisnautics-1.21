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

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.util.PipeConnectionsC;
import com.neep.meatlib.util.graph.PipeVertex;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.meatlib.util.PipeConnections;
import com.neep.neepmeat.transport.block.item_transport.OpaqueItemPipeBlock;
import com.neep.neepmeat.transport.item_network.ItemInPipe;
import com.neep.neepmeat.transport.item_network.ItemPipeVertexImpl;
import com.neep.neepmeat.transport.item_network.RoutingNetworkCache;
import com.neep.neepmeat.transport.item_network.ShittyRoutingCallbackHolder;
import com.neep.neepmeat.transport.util.ItemPipeUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ItemPipeBlockEntity extends SyncableBlockEntity implements NetworkTrackingItemPipe
{
    public static int MAX_ITEMS = 300;

    protected List<ItemInPipe> items = new ArrayList<>();
    protected RoutingNetworkCache cache = new RoutingNetworkCache();
    protected final List<BlockApiCache<Storage<ItemVariant>, Direction>> storageCaches = new ObjectArrayList<>(Collections.nCopies(6, null));
    public int lastOutput;

    protected final ItemPipeVertexImpl vertex;

    protected final ShittyRoutingCallbackHolder callbackHolder = new ShittyRoutingCallbackHolder();

    public ItemPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        this(type, pos, state, ItemPipeVertexImpl::new);
    }

    public ItemPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, PipeConstructor<?> constructor)
    {
        super(
                // This is a stopgap measure for ensuring that opaque pipes from previous versions are properly updated
                state.getBlock() instanceof OpaqueItemPipeBlock && type != NMBlockEntities.OPAQUE_ITEM_PIPE ? NMBlockEntities.OPAQUE_ITEM_PIPE : type,
                pos, state);

        this.vertex = constructor.create(this);
    }

    public List<ItemInPipe> getItems()
    {
        return items;
    }

    public ShittyRoutingCallbackHolder getCallbacks()
    {
        return callbackHolder;
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);

        ListTag itemList = new ListTag();
        for (ItemInPipe offset : items)
        {
            CompoundTag nbt1 = new CompoundTag();
            nbt1 = offset.toNbt(nbt1);
            itemList.add(nbt1);
        }

        tag.put("items", itemList);

        tag.put("vertex", vertex.writeNbt(new CompoundTag()));
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);

        ListTag itemList = (ListTag) tag.get("items");
        int size = itemList != null ? itemList.size() : 0;
        items.clear();
        for (int i = 0; i < size; ++i)
        {
            items.add(ItemInPipe.fromNbt(itemList.getCompound(i)));
        }

        // World migration
        if (!tag.contains("vertex"))
            vertex.needsMigration = true;

        vertex.readNbt(tag.getCompound("vertex"));
    }

    public void serverTick(Level world, BlockPos pos, BlockState state)
    {
        int numItems = items.size();
        if (numItems == 0)
            return;

        Iterator<ItemInPipe> it = items.listIterator();
        while (it.hasNext())
        {
            ItemInPipe item = it.next();
            item.tick();
            if (item.progress >= 1)
            {
                try (Transaction transaction = Transaction.openOuter())
                {
                    long transferred = pipeToAny(item, pos, item.out, world, transaction);
                    if (transferred == item.amount() || item.isEmpty())
                    {
                        it.remove();
                    }
                    else
                    {
                        ItemPipeUtil.bounce(item, world, state);
                    }
                    transaction.commit();
                }
            }
        }

        sync();
    }

    private long pipeToAny(ItemInPipe item, BlockPos pos, Direction out, Level world, TransactionContext transaction)
    {
        BlockPos toPos = pos.relative(out);
        BlockState toState = world.getBlockState(toPos);
        Block toBlock = toState.getBlock();

        long amountInserted = 0;
        Storage<ItemVariant> storage;
        if (toBlock instanceof ItemPipe pipe)
        {
            amountInserted = ItemPipeUtil.itemToPipe(item, pipe, (ServerLevel) world, toPos, toState, out, transaction);
        }
        else if (ItemPipeUtil.canDumpInto(world, toPos, toState))
        {
            amountInserted = ItemPipeUtil.itemToWorld(item, 0.2, item.speed, world, toPos, out, transaction);
        }
        else if ((storage = getStorage(out)) != null)
        {
            amountInserted = ItemPipeUtil.itemToStorage(item, storage, transaction);
        }

        // TODO: is this condition necessary?
        if (amountInserted != item.amount())
        {
            item.decrement((int) amountInserted);
        }

        return amountInserted;
    }

    public long insert(ItemInPipe item, Level world, BlockState state, BlockPos pos, Direction in, TransactionContext transaction)
    {
        if (item.isEmpty())
        {
            NeepMeat.LOGGER.error("Pipe at {}: Tried to insert empty item variant", pos);
            return 0;
        }

        Direction out = ((ItemPipe) getBlockState().getBlock()).getOutputDirection(item, pos, state, world, in,
                this, !vertex.canSimplify(), transaction);

        transaction.addCloseCallback(new InsertCallback(item, in, out, world, this));

        return item.amount();
    }

    protected void insertItemCallback(ItemInPipe item, Direction in, Direction out, Level world)
    {
        if (items.size() < MAX_ITEMS)
        {
            item.reset(in, out, world.getGameTime());
            this.items.add(item);
        }

        // Void the item. There's no reason why there would be 300 items in a pipe in a legitimate use.
    }

    public void dropItems()
    {
        for (ItemInPipe item : items)
        {
            Entity itemEntity = new ItemEntity(getLevel(), getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5, item.toItemStack());
            item.zero();
            getLevel().addFreshEntity(itemEntity);
        }
        items.clear();
    }

    @Override
    public RoutingNetworkCache getCache()
    {
        return cache;
    }

    public BlockApiCache<Storage<ItemVariant>, Direction> getStorageCache(Direction localDirection)
    {
        int id = localDirection.ordinal();
        if (storageCaches.get(id) == null)
        {
            storageCaches.set(id, BlockApiCache.create(ItemStorage.SIDED, (ServerLevel) level, worldPosition.relative(localDirection)));
        }
        return storageCaches.get(id);
    }

    @Nullable
    public Storage<ItemVariant> getStorage(Direction localDirection)
    {
        return getStorageCache(localDirection).find(localDirection.getOpposite());
    }

    public int getCurrentOutput(PipeConnectionsC connections)
    {
        if (!connections.contains(Direction.values()[lastOutput]))
            return nextOutput(connections);

        return lastOutput;
    }

    public void setOutput(int dir)
    {
        lastOutput = dir;
    }

    public int nextOutput(PipeConnectionsC connections)
    {
        lastOutput = (lastOutput + 1) % 6;
        int i = 0;
        while (!connections.contains(Direction.values()[lastOutput]))
        {
            lastOutput = (lastOutput + 1) % 6;

            // If a pipe mysteriously has no outputs
            if (i >= 6)
                return 0;

            ++i;
        }

        return lastOutput;
    }

    public PipeVertex getVertex()
    {
        return vertex;
    }

    private record InsertCallback(ItemInPipe item, Direction in, Direction out, Level world,
                                  ItemPipeBlockEntity be) implements TransactionContext.CloseCallback
    {
        public void onClose(TransactionContext transaction, TransactionContext.Result result)
        {
            if (result.wasCommitted())
            {
                if (transaction.nestingDepth() > 0)
                {
                    transaction.getOpenTransaction(transaction.nestingDepth() - 1).addCloseCallback(this);
                }
                else
                {
                    transaction.addOuterCloseCallback(r ->
                    {
                        if (!r.wasCommitted())
                            return;

                        be.insertItemCallback(item, in, out, world);
                    });
                }
            }
        }
    }

    @FunctionalInterface
    public interface PipeConstructor<T extends ItemPipeVertexImpl>
    {
        T create(ItemPipeBlockEntity parent);
    }
}

