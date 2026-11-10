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

package com.neep.neepmeat.transport.block.item_transport;

import com.neep.neepmeat.init.NMParticles;
import com.neep.neepmeat.transport.ItemTransport;
import com.neep.neepmeat.transport.api.item_network.Craftable;
import com.neep.neepmeat.transport.api.item_network.CraftingPredicate;
import com.neep.neepmeat.transport.api.item_network.RoutablePipe;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.neepmeat.transport.block.item_transport.entity.NetworkTrackingItemPipe;
import com.neep.neepmeat.transport.fluid_network.node.NodePos;
import com.neep.neepmeat.transport.interfaces.IServerWorld;
import com.neep.neepmeat.transport.item_network.ItemRoute;
import com.neep.neepmeat.transport.item_network.PipeCacheImpl;
import com.neep.neepmeat.transport.util.BOM;
import com.neep.neepmeat.util.BFSGroupFinder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.ResourceAmount;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class RoutingNetworkImpl implements RoutingNetwork
{
    protected boolean needsUpdate = true;
    protected final GroupFinder finder = new GroupFinder();

    protected long version;

    protected Supplier<ServerLevel> worldSupplier;
    protected final BlockPos pos;

//    protected final ObjectList<BlockApiCache<RoutablePipe, Direction>> routablePipes = new ObjectArrayList<>(10);
    protected final Long2ObjectMap<BlockApiCache<RoutablePipe, Direction>> routePipeMap = new Long2ObjectOpenHashMap<>();

    protected final List<RoutablePipe> routePipes = new ObjectArrayList<>(); // Sorted with out priority
    protected final List<RoutablePipe> insertRoutePipes = new ObjectArrayList<>(); // Sorted with in priority
    protected boolean valid;

    public List<DeferredRequest> getActiveRequests()
    {
        return deferredRequests;
    }

    private final List<DeferredRequest> deferredRequests = new ObjectArrayList<>();

    public RoutingNetworkImpl(BlockPos pos, Supplier<ServerLevel> worldSupplier)
    {
        this.pos = pos;
        this.worldSupplier = worldSupplier;
    }

    public static <T> ResourceAmount<T> viewToAmount(StorageView<T> view)
    {
        return new ResourceAmount<>(view.getResource(), view.getAmount());
    }

    @Override
    public void invalidate()
    {
        needsUpdate = true;
        ++version;
    }

    @Override
    public boolean needsUpdate()
    {
        return needsUpdate;
    }

    @Override
    public void update()
    {
        needsUpdate = false;

        routePipeMap.clear();
        routePipes.clear();
        insertRoutePipes.clear();

        finder.reset();
        finder.queueBlock(pos);
        finder.loop(ItemTransport.BFS_MAX_DEPTH);

        if (finder.controllers > 1)
        {
            valid = false;
            return;
        }

        finder.getResult().forEach((k, v) -> routePipeMap.put(k.asLong(), v));

        // Build the flat pipe cache. Hopefully the network is invalidated enough to ensure that the BlockApiCaches will never be empty null.
        routePipeMap.values()
                .stream()
                .map(c -> c.find(null))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(RoutablePipe::outPriority).reversed())
                .forEachOrdered(routePipes::add);
        routePipeMap.values()
                .stream()
                .map(c -> c.find(null))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(RoutablePipe::inPriority).reversed())
                .forEachOrdered(insertRoutePipes::add);

        // Set networks
        finder.getVisited().forEach(p ->
        {
            if (worldSupplier.get().getBlockEntity(BlockPos.of(p)) instanceof NetworkTrackingItemPipe be)
            {
                be.getCache().setNetwork(worldSupplier.get(), RoutingNetworkImpl.this.pos);
            }
        });

        valid = true;
    }

    // Sorts the pipes in descending order
    private static int compare(RoutablePipe p0, RoutablePipe p1)
    {
        int x = p0.outPriority();
        int y = p1.outPriority();
        return Integer.compare(y, x);
    }

    @Override
    public long getVersion()
    {
        return version;
    }

    @Override
    public boolean isValid()
    {
        return valid;
    }

    @Override
    public void serverTick()
    {
        // Requester makes a request
        // Network checks whether anything can fulfil the request
        // Network waits for the thing to fulfil the request
        // Network routes results to the requester

        Level world = worldSupplier.get();
        if (world.getGameTime() % 10 == 0)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                var it = deferredRequests.iterator();
                while (it.hasNext())
                {
                    var request = it.next();
                    var pipe = request.getPipe(worldSupplier.get());
                    if (pipe == null)
                    {
                        // Handle removal of pipes from the world
                        it.remove();
                    }
                    else
                    {
                        RoutablePipe.DeferredResult result = pipe.deferredUpdate(request, this, transaction);
                        if (result == RoutablePipe.DeferredResult.SUCCESS || result == RoutablePipe.DeferredResult.FAIL)
                        {
                            it.remove();
                        }
                    }
                }
                transaction.commit();
            }
        }
    }

    @Override
    public Stream<ResourceAmount<ItemVariant>> getAllAvailable(Predicate<ItemVariant> predicate, TransactionContext transaction)
    {
        return routePipes.stream()
                .flatMap(p -> p.getAvailable(transaction))
                .filter(v -> predicate.test(v.getResource()))
                .map(RoutingNetworkImpl::viewToAmount);
    }

    @Override
    public Stream<Craftable> getAllCraftable(Predicate<ItemVariant> predicate, TransactionContext transaction)
    {
        return routePipes.stream().flatMap(p -> p.getCraftable(transaction));
    }

    public Stream<Craftable.ProvisionContext.View> getAllAvailableViews(TransactionContext transaction)
    {
        return routePipes.stream()
                .flatMap(p -> p.getAvailable(transaction)
                        .filter(v -> !v.isResourceBlank())
                        .map(v -> new Craftable.ProvisionContext.View(v.getResource(), v.getAmount(), p, false)));
    }

    @Override
    public boolean route(Predicate<ItemVariant> predicate, long amount, BlockPos inPos, Direction inDir, BlockPos outPos, Direction outDir, RequestType type, TransactionContext transaction)
    {
        amount = Math.max(0, amount);

        try (Transaction inner = transaction.openNested())
        {
            var inPipeCache = routePipeMap.get(inPos.asLong());
            if (inPipeCache != null)
            {
                RoutablePipe inPipe = inPipeCache.find(null);
                if (inPipe != null)
                {
                    RoutablePipe.Result routed = inPipe.request(predicate, amount, new NodePos(outPos, outDir), new RequestContext(), inner);
                    if (type.satisfied(amount, routed.total()))
                    {
                        successAnimation();
                        inner.commit();
                        return true;
                    }
                }
            }
            inner.abort();
        }
        return false;
    }

    @Override
    public boolean request(Predicate<ItemVariant> predicate, long amount, BlockPos pos, Direction outDir, RequestType type, RequestContext context, TransactionContext transaction)
    {
        if (context.recursionDepth > 30)
        {
            failAnimation();
            return false;
        }

        amount = Math.max(0, amount);
        if (amount == 0)
            return true;

        // This could be replaced with requestFuture for conciseness, but that adds a bit of complexity as it expects a list of every available resource.
        // A single request for a real item doesn't need all that and can be resolved instantly.
        try (Transaction inner = transaction.openNested())
        {
            AtomicLong amountRemaining = new AtomicLong(amount);

            for (RoutablePipe e : routePipes)
            {
                context.recursionDepth++;
                long retrieved = e.request2(predicate, amountRemaining.get(), new NodePos(pos, outDir), inner);
                amountRemaining.addAndGet(-retrieved);

                context.recursionDepth--;

                if (amountRemaining.get() <= 0)
                {
                    break;
                }
            }

            // We need to craft something
            if (amountRemaining.get() > 0)
            {
                BOM bom = new BOM();
                Craftable.ProvisionContext provisionContext = new Craftable.ProvisionContext(
                        getAllAvailableViews(inner),
                        routePipes.stream().flatMap(p -> p.getCraftable(inner)).toList()
                );

                long requested = requestFuture(predicate, amountRemaining.get(), new NodePos(pos, outDir), bom, provisionContext, false, inner);

                amountRemaining.addAndGet(-requested);

                if (amountRemaining.get() == 0)
                {
                    // Deferred requests
                    for (var operation : bom.deferredOperations())
                    {
                        deferredRequests.add(new DeferredRequest(operation.source().getRoutablePipePos(), operation.dest().pos(), operation.dest().face(),
                                operation.amount(), operation.resource()));
                    }

                    // Immediate requests
                    for (var operation : bom.operations())
                    {
                        // Now comes the sticky part
                        // Until now, each routable pipe has had its own implementation for doing this
                        operation.source().request2(v -> v.equals(operation.resource()), operation.amount(), operation.dest(), inner);
                    }

                    // Notify the crafting participants if they want to await ingredients
                    for (var craft : bom.crafts())
                    {
                        craft.source().notifyCraft(craft.result(), craft.dependencies(), craft.batches());
                    }
                }
            }

            if (type.satisfied(amount, amount - amountRemaining.get()))
            {
                if (context.recursionDepth == 0)
                {
                    successAnimation();
                }

                inner.commit();
                return true;
            }
            else
            {
                failAnimation();
                inner.abort();
            }
        }

        return false;
    }

    public long store(ItemVariant variant, long amount, BlockPos inPos, Direction inFace, TransactionContext transaction)
    {
        // inPos corresponds to the block that is ejecting, so this is the first actual pipe.
        BlockPos inPipe = inPos.relative(inFace);

        PipeCacheImpl itemNetwork = ((IServerWorld) worldSupplier.get()).getItemNetwork();
        StoreRouter storeRouter = new StoreRouter()
        {
            @Override
            public ItemRoute findPathTo(BlockPos toPos, Direction outDir, long amount)
            {
                return itemNetwork.findPath(new ResourceAmount<>(variant, amount), inPipe, toPos, outDir);
            }

            @Override
            public long addOperation(ItemRoute route, ItemVariant variant, long maxAmount)
            {
                return itemNetwork.route(inPos, inFace, route, variant, maxAmount, transaction);
            }
        };

        long amountRemaining = amount;
        for (RoutablePipe e : insertRoutePipes)
        {
            long insertable = e.store(variant, amountRemaining, storeRouter, transaction);
            amountRemaining -= insertable;

            if (amountRemaining <= 0)
                break;
        }

        if (amount - amountRemaining > 0)
            successAnimation();
        else
            failAnimation();

        return amount - amountRemaining;
    }

    /// Recursively resolves the crafting dependencies (essentially a bill of materials) for the given item predicate.
    /// Deals with the following:
    /// - Predicting how many of a particular resource is present throughout the process
    /// - Recursive dependencies (obviously)
    /// - Extra results from recipes (minimizes wastage of input materials)
    /// - Some other things probably
    private long requestFuture(Predicate<ItemVariant> predicate, long amount, NodePos dest, BOM bom, Craftable.ProvisionContext provisionContext, boolean immediate, TransactionContext transaction)
    {
        AtomicLong amountRemaining = new AtomicLong(amount);

        // Try to fulfil with existing items first.
        if (immediate && amountRemaining.get() > 0)
        {
            provisionContext.getReal(predicate).anyMatch(view ->
            {
                long extractable = view.extract(amountRemaining.get());
                if (extractable > 0)
                {
                    amountRemaining.addAndGet(-extractable);

                    // Queue the actual transfer
                    // Note that the predicate is replaced by the resource that it matches at this point
                    bom.addOperation(view.underlying(), dest, view.resource(), extractable, view.future);
                }

                return amountRemaining.get() <= 0;
            });
        }

        // Request cannot be fulfilled using existing items. Check which recipes can provie it.
        if (amountRemaining.get() > 0)
        {
            // TODO: loop through multiple craftables
            var view = provisionContext.getCraftable(predicate, amountRemaining.get());
            if (view != null)
            {
                long craftable = 0; // TODO: oh no
                var dependencies = view.getDependencies(amountRemaining.get(), transaction);
                if (dependencies == null)
                {
                    // TODO: continue
                    return 0;
                }

                for (RoutablePipe.Dependency d : dependencies.dependencies())
                {
                    var pipe = view.underlying(); // This pipe is the destination of dependency requests and crafts
                    var outFace = pipe.requestDestPosDir(); // The direction that the pipe expects the ingredients for this craft

                    if (outFace == null) // The pipe might be set up incorrectly
                        return 0;

                    long required = d.amount();

                    long satisfied = requestFuture(d.predicate(), required, outFace, bom, provisionContext, true, transaction);
                    if (satisfied != required)
                    {
                        // I think this is right (currently).
                        return 0;
                    }
                }

                // At this point, all dependencies should be confirmed as present in the required amounts.

                // Queue a craft notification
                // This can be used check when all dependencies are present
                bom.addCraftNotification(view.underlying(), dependencies.dependencies(), new ResourceAmount<>(view.result().resource(),
                        view.result().amount() * amountRemaining.get()), dependencies.batches());

                // Keep track of remainders to minimise initial cost
                // TODO: this is wrong because it assumes that the buffered resources are immediately available
                provisionContext.insert(view.underlying(), view.result().resource(), dependencies.extra());

                craftable = amountRemaining.get();
                amountRemaining.set(0);

                if (amountRemaining.get() == 0)
                {
                    // Send the result to dest
                    bom.addOperation(view.underlying(), dest, view.result().resource(), craftable, true);
                    return amount;
                }
                else
                {
                    // Otherwise, there's definitely nothing that can provide the resource.
                    return 0;
                }
            }
            else
            {
                long initial = provisionContext.getReal(predicate).mapToLong(v -> v.initialAmount).sum();
                PlayerLookup.around(worldSupplier.get(), pos, 10).forEach(p ->
                {
                    p.sendSystemMessage(Component.literal("Request failed: no provider for %s × %s".formatted(amountRemaining.get() + initial, CraftingPredicate.format(predicate))));
                });
                return 0;
            }
        }
        else
        {
            return amount;
        }
    }

    private void failAnimation()
    {
        worldSupplier.get().playSound(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(), SoundEvents.ZOMBIFIED_PIGLIN_HURT, SoundSource.BLOCKS, 1, 1);
        worldSupplier.get().sendParticles(NMParticles.BLOOD_SHOWER, this.pos.getX() + 0.5, this.pos.getY() + 1, this.pos.getZ() + 0.5, 20, 0.1, 0.5, 0.1, 0.1);
    }

    private void successAnimation()
    {
        worldSupplier.get().sendParticles(ParticleTypes.SMOKE, this.pos.getX() + 0.5, this.pos.getY() + 1, this.pos.getZ() + 0.5, 20, 0.1, 0, 0.1, 0.01);
        worldSupplier.get().playSound(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(), SoundEvents.PIGLIN_CELEBRATE, SoundSource.BLOCKS, 1, 1);
    }

    public void removeDeferredFrom(List<BlockPos> pos)
    {
        deferredRequests.removeIf(r -> pos.contains(r.requesterPos));
    }

    public CompoundTag writeNbt(CompoundTag nbt)
    {
        ListTag list = new ListTag();
        for (DeferredRequest deferredRequest : deferredRequests)
        {
            list.add(deferredRequest.toNbt());
        }

        nbt.put("deferred", list);
        return nbt;
    }

    public void readNbt(CompoundTag nbt)
    {
        var list = nbt.getList("deferred", Tag.TAG_COMPOUND);
        deferredRequests.clear();
        for (int i = 0; i < list.size(); i++)
        {
            CompoundTag nbtCompound = list.getCompound(i);
            deferredRequests.add(i, DeferredRequest.fromNbt(nbtCompound));
        }
    }

    public interface StoreRouter
    {
        ItemRoute findPathTo(BlockPos toPos, Direction outDir, long amount);
        long addOperation(ItemRoute route, ItemVariant variant, long maxAmount);
    }

    protected class GroupFinder extends BFSGroupFinder<BlockApiCache<RoutablePipe, Direction>>
    {
        protected int controllers = 0;

        @Override
        public void reset()
        {
            super.reset();
            controllers = 0;
        }

        public Set<Long> getVisited()
        {
            return visited;
        }

        @Override
        protected State processPos(BlockPos pos)
        {
            ItemPipe fromPipe = ItemTransport.ITEM_PIPE_LOOKUP.find(worldSupplier.get(), pos, null);

            // Fail if there is a second controller in the network.
            if (checkController(worldSupplier.get(), pos)) return State.FAIL;

            BlockPos.MutableBlockPos mutable = pos.mutable();
            for (Direction direction : fromPipe.getConnections(worldSupplier.get().getBlockState(pos), null))
            {
                mutable.setWithOffset(pos, direction);
                RoutablePipe routablePipe = RoutablePipe.LOOKUP.find(worldSupplier.get(), mutable, null);
                if (routablePipe != null)
                {
                    addResult(mutable, BlockApiCache.create(RoutablePipe.LOOKUP, worldSupplier.get(), mutable));
                }

                ItemPipe toPipe = ItemTransport.ITEM_PIPE_LOOKUP.find(worldSupplier.get(), mutable, null);
                if (toPipe != null)
                {
                    queueBlock(mutable);
                }
            }

            return State.CONTINUE;
        }

        protected boolean checkController(ServerLevel world, BlockPos current)
        {
            BlockApiCache<RoutingNetwork, Void> cache = BlockApiCache.create(RoutingNetwork.LOOKUP, (ServerLevel) world, current);
            if (cache.find(null) != null)
            {
                ++controllers;
            }
            return false;
        }
    }
}
