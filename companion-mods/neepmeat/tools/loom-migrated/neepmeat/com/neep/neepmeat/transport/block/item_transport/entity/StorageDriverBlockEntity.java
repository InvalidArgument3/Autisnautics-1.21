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
import com.neep.neepmeat.transport.ItemTransport;
import com.neep.neepmeat.transport.api.item_network.PriorityPipe;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.api.item_network.StorageDriver;
import com.neep.neepmeat.transport.block.item_transport.RoutingNetworkImpl;
import com.neep.neepmeat.transport.fluid_network.node.NodePos;
import com.neep.neepmeat.transport.interfaces.IServerWorld;
import com.neep.neepmeat.transport.item_network.ItemRoute;
import com.neep.neepmeat.transport.item_network.PipeCacheImpl;
import com.neep.neepmeat.transport.item_network.RetrievalTarget;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class StorageDriverBlockEntity extends ItemPipeBlockEntity implements StorageDriver, PriorityPipe
{
    protected final List<RetrievalTarget<ItemVariant>> targets = new ArrayList<>(6);
    protected boolean needsUpdate = true;
    private int outPriority;
    private int inPriority;

    public StorageDriverBlockEntity(BlockPos pos, BlockState state)
    {
        this(ItemTransport.STORAGE_BUS_BE, pos, state);
    }

    public StorageDriverBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    protected void updateTargets()
    {
        targets.clear();

        BlockPos.MutableBlockPos mutable = getBlockPos().mutable();
        for (Direction direction : Direction.values())
        {
            mutable.setWithOffset(getBlockPos(), direction);
            BlockApiCache<Storage<ItemVariant>, Direction> storageCache = BlockApiCache.create(ItemStorage.SIDED, (ServerLevel) level, mutable);
            if (storageCache.find(direction.getOpposite()) != null)
            {
                targets.add(RetrievalTarget.of(storageCache, direction.getOpposite()));
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putInt("out_priority", outPriority);
        nbt.putInt("in_priority", inPriority);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.outPriority = nbt.getInt("out_priority");
        this.inPriority = nbt.getInt("in_priority");
    }

    @Override
    public void update(ServerLevel world, BlockPos pos)
    {
        updateTargets();
    }

    @Override
    public List<RetrievalTarget<ItemVariant>> getTargets()
    {
        if (needsUpdate)
        {
            updateTargets();
//            if (getWorld() != null && getWorld() instanceof ServerWorld)
//            {
//                targets.clear();
//                storageFaces.forEach(p -> targets.add(new RetrievalTarget<>(BlockApiCache.create(ItemStorage.SIDED, (ServerWorld) getWorld(), p.first()), p.second())));
                needsUpdate = false;
//            }
        }
        return targets;
    }

    @Override
    public Result request(Predicate<ItemVariant> predicate, long amount, NodePos fromPos, RoutingNetwork.RequestContext context, TransactionContext transaction)
    {
        AtomicLong available = new AtomicLong();

        long remaining1 = amount;
        var views = getAvailable(transaction).filter(v -> predicate.test(v.getResource())).toList();
        for (var view : views)
        {
            remaining1 = Math.max(0, remaining1 - view.getAmount());
            available.addAndGet(view.getAmount());

            if (remaining1 <= 0)
            {
                break;
            }
        }

        RoutingOperation operation = t ->
        {
            PipeCacheImpl itemNetwork = ((IServerWorld) level).getItemNetwork();
            AtomicLong routed = new AtomicLong();

            long remaining = amount;
            for (RetrievalTarget<ItemVariant> target : getTargets())
            {
                Storage<ItemVariant> storage = target.find();
                if (storage == null)
                    continue;

                ItemVariant extractable = null;
                for (var view : storage)
                {
                    ItemVariant resource = view.getResource();
                    if (view.getAmount() > 0 && !view.isResourceBlank() && predicate.test(resource))
                        extractable = resource;
                }

                if (extractable == null)
                    continue;

                long extracted = storage.extract(extractable, amount, t);

                remaining -= extracted;
                if (extracted > 0)
                {
                    ItemRoute route = itemNetwork.findPath(new ResourceAmount<>(extractable, amount), worldPosition, fromPos.pos(), fromPos.face());
                    long l = itemNetwork.route(target.getPos(), target.accessFace(), route, extractable, extracted, t);
                    routed.addAndGet(l);
                }

                if (remaining <= 0)
                    break;
            }

            return routed.get();
        };

        return Result.immediate(operation, available.get());
    }

    @Override
    public long request2(Predicate<ItemVariant> predicate, long amount, NodePos dest, TransactionContext transaction)
    {
        PipeCacheImpl itemNetwork = ((IServerWorld) level).getItemNetwork();
        AtomicLong routed = new AtomicLong();

        long remaining = amount;
        for (RetrievalTarget<ItemVariant> target : getTargets())
        {
            Storage<ItemVariant> storage = target.find();
            if (storage == null)
                continue;

            ItemVariant extractable = null;
            for (var view : storage)
            {
                ItemVariant resource = view.getResource();
                if (view.getAmount() > 0 && !view.isResourceBlank() && predicate.test(resource))
                    extractable = resource;
            }

            if (extractable == null)
                continue;

            long extracted = storage.extract(extractable, amount, transaction);

            remaining -= extracted;
            if (extracted > 0)
            {
                ItemRoute route = itemNetwork.findPath(new ResourceAmount<>(extractable, amount), worldPosition, dest.pos(), dest.face());
                long l = itemNetwork.route(target.getPos(), target.accessFace(), route, extractable, extracted, transaction);
                routed.addAndGet(l);
            }

            if (remaining <= 0)
                break;
        }

        return routed.get();
    }

    @Override
    public Stream<StorageView<ItemVariant>> getAvailable(TransactionContext transaction)
    {
        return getTargets().stream().map(RetrievalTarget::find).filter(Objects::nonNull).flatMap(Streams::stream);
    }

    @Override
    public long store(ItemVariant variant, long maxAmount, RoutingNetworkImpl.StoreRouter router, TransactionContext transaction)
    {
        long amountRemaining = maxAmount;

        for (RetrievalTarget<ItemVariant> target : getTargets())
        {
            var storage = target.find();
            if (storage == null)
                continue;

            long insertable;
            try (Transaction inner = transaction.openNested())
            {
                insertable = storage.insert(variant, amountRemaining, inner);
                inner.abort();
            }

            if (insertable > 0)
            {
                var route = router.findPathTo(worldPosition, target.accessFace().getOpposite(), insertable);
                long routable = router.addOperation(route, variant, insertable);
                amountRemaining -= routable;
            }

            if (amountRemaining <= 0)
            {
                return maxAmount;
            }
        }

//        var route = itemNetwork.findPath(new ResourceAmount<>(variant, insertable), inPos.offset(inFace), e.getRoutablePipePos(), endFace); // TODO: direction

        return maxAmount - amountRemaining;
    }

    @Override
    public @Nullable NodePos requestDestPosDir()
    {
        // Why are you doing this?
        return null;
    }

    @Override
    public BlockPos getRoutablePipePos()
    {
        return worldPosition;
    }

    @Override
    public void setOutputPriority(int outputPriority, int inputPriority)
    {
        this.outPriority = outputPriority;
        this.inPriority = inputPriority;
        cache.update();
        setChanged();
    }

    @Override
    public int getOutputPriority()
    {
        return outPriority;
    }

    @Override
    public int getInputPriority()
    {
        return inPriority;
    }

    @Override
    public int outPriority()
    {
        return outPriority;
    }

    @Override
    public int inPriority()
    {
        return inPriority;
    }
}
