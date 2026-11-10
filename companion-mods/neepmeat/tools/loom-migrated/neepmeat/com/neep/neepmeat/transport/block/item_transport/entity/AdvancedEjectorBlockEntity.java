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

import com.google.common.collect.Streams;
import com.neep.meatlib.util.RedstoneMode;
import com.neep.neepmeat.api.storage.DynamicLazyBlockApiCache;
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import com.neep.neepmeat.item.filter.FilterList;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.neepmeat.transport.block.item_transport.machine.AdvancedEjectorBlock;
import com.neep.neepmeat.transport.block.item_transport.machine.ItemEjector;
import com.neep.neepmeat.transport.item_network.ItemInPipe;
import com.neep.neepmeat.transport.item_network.ItemPipeVertexImpl;
import com.neep.neepmeat.transport.item_network.PipeRouteFinder;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AdvancedEjectorBlockEntity extends ItemPipeBlockEntity implements ItemEjector
{
    protected final LazyBlockApiCache<Storage<ItemVariant>, Direction> extractionCache = addListener(DynamicLazyBlockApiCache.facing(ItemStorage.SIDED, this,
            () -> getBlockState().getValue(AdvancedEjectorBlock.FACING)));

    protected int counter;

    protected FilterList filterList = new FilterList(2, this::setChanged);

    private boolean roundRobin;
    private int ejectAmount = 16;
    private int period = 10;
    private int roundRobinIndex;
    private RedstoneMode redstoneMode = RedstoneMode.ACTIVE_WITH;

    private boolean active;

    public AdvancedEjectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state, Vertex::new);
    }

    @Override
    public void serverTick(Level world, BlockPos pos, BlockState state)
    {
        if (counter <= 0 && active)
        {
            tryTransfer();
            counter = period;
        }

        counter = Math.max(0, counter -1);

        super.serverTick(world, pos, state);
    }

    protected void tryTransfer()
    {
        Storage<ItemVariant> storage;
        if ((storage = extractionCache.find()) != null)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                StorageView<ItemVariant> matchingView = findResource(storage);

                if (matchingView == null || matchingView.isResourceBlank())
                   return;

                Direction in = extractionCache.ctx().getOpposite();

                ItemVariant resource = matchingView.getResource();
                long extractable = matchingView.getAmount();

                var result = new PipeRouteFinder((ServerLevel) level, worldPosition).run(new ResourceAmount<>(resource, Math.min(ejectAmount, extractable)), s -> s != storage, in, transaction);
                long canRoute = Math.min(ejectAmount, result.maxAmount());

                long extracted = matchingView.extract(resource, canRoute, transaction);

                ItemInPipe item = new ItemInPipe(resource, (int) extracted);
                // TODO: Implement round-robin and use the route
//                item.setRoute(result.firstRoute());

                Direction out = ((ItemPipe) getBlockState().getBlock()).getOutputDirection(item, worldPosition, getBlockState(), level, in, this,
                        !vertex.canSimplify(), transaction);

                if (extracted > 0)
                {
                    boolean inserted = insert(item, in, out);

                    if (inserted)
                    {
                        transaction.commit();
                        return;
                    }
                }

                transaction.abort();
            }
        }
    }

    @Nullable
    private StorageView<ItemVariant> findResource(Storage<ItemVariant> storage)
    {
        if (roundRobin)
        {
            if (storage instanceof InventoryStorage inventoryStorage)
            {
                int size = inventoryStorage.getSlotCount();

                int i = 0;

                do
                {
                    roundRobinIndex = Mth.clamp((roundRobinIndex + 1) % size, 0, size);
                    var view = inventoryStorage.getSlot(roundRobinIndex);

                    if (!view.isResourceBlank() && filterList.matches(view.getResource()))
                        return view;

                    i++;
                }
                while (i < size);
            }
            else
            {
                // This should reduce the number of times we use the potentially slow iterator implemented in the storage (?)
                List<StorageView<ItemVariant>> views = Streams.stream(storage).toList();
                int size = views.size();

                int i = 0;

                do
                {
                    roundRobinIndex = Mth.clamp((roundRobinIndex + 1) % size, 0, size);
                    var view = views.get(roundRobinIndex);

                    if (!view.isResourceBlank() && filterList.matches(view.getResource()))
                        return view;

                    i++;
                }
                while (i < size);

            }

        }
        else
        {
            for (StorageView<ItemVariant> view : storage.nonEmptyViews())
            {
                if (filterList.matches(view.getResource()))
                {
                    return view;
                }
            }
        }

        return null;
    }

    // Using boolean return value because it will be much more convoluted to support partial insertion.
    private boolean insert(ItemInPipe item, Direction in, Direction out)
    {
        if (items.size() < MAX_ITEMS)
        {
            item.reset(in, out, level.getGameTime());
            this.items.add(item);

            return true;
        }

        return false;
    }

    public FilterList getFilters()
    {
        return filterList;
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        filterList.readNbt(tag);
        this.roundRobin = tag.getBoolean("round_robin");
        this.ejectAmount = tag.getInt("eject_amount");
        this.period = tag.getInt("period");
        this.redstoneMode = RedstoneMode.values()[tag.getInt("redstone_mode")];
        this.roundRobinIndex = tag.getInt("round_robin_index");
        this.active = tag.getBoolean("active");
    }

    @Override
    public void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        filterList.writeNbt(tag);
        tag.putBoolean("round_robin", roundRobin);
        tag.putInt("eject_amount", ejectAmount);
        tag.putInt("period", period);
        tag.putInt("redstone_mode", redstoneMode.ordinal());
        tag.putInt("round_robin_index", roundRobinIndex);
        tag.putBoolean("active", active);
    }

    @Override
    public boolean roundRobin()
    {
        return roundRobin;
    }

    @Override
    public int amount()
    {
        return ejectAmount;
    }

    @Override
    public int period()
    {
        return period;
    }

    @Override
    public RedstoneMode redstoneMode()
    {
        return redstoneMode;
    }

    @Override
    public void update(boolean roundRobin, int amount, int period, RedstoneMode redstoneMode)
    {
        this.roundRobin = roundRobin;
        this.ejectAmount = Mth.clamp(amount, 0, 64);
        this.period = Mth.clamp(period, 10, 100);
        this.redstoneMode = redstoneMode;
        setChanged();
        updateRedstone(level.hasNeighborSignal(worldPosition));
    }

    public void updateRedstone(boolean receivingRedstonePower)
    {
        this.active = redstoneMode.active(receivingRedstonePower);
    }

    private static class Vertex extends ItemPipeVertexImpl
    {
        public Vertex(BlockEntity parent)
        {
            super(parent);
        }

        @Override
        public boolean canSimplify()
        {
            return false;
        }
    }
}
