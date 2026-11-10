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

package com.neep.neepmeat.plc.block.entity;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.neepmeat.api.plc.interrupt.InterruptEmitter;
import com.neep.neepmeat.api.plc.interrupt.InterruptReceiver;
import com.neep.neepmeat.api.plc.memory.MemoryProvider;
import com.neep.neepmeat.api.storage.DynamicLazyBlockApiCache;
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.plc.block.ProcessInterfaceBlock;
import com.neep.neepmeat.plc.instruction.PlcInstruction;
import com.neep.neepmeat.plc.memory.MemoryEntry;
import com.neep.neepmeat.transport.api.item_network.Craftable;
import com.neep.neepmeat.transport.api.item_network.CraftingPredicate;
import com.neep.neepmeat.transport.api.item_network.RoutablePipe;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.fluid_network.node.NodePos;
import com.neep.neepmeat.transport.interfaces.IServerWorld;
import com.neep.neepmeat.transport.item_network.ItemRoute;
import com.neep.neepmeat.transport.item_network.PipeCacheImpl;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import com.neep.meatlib.transfer.InventoryStorage;
import com.neep.meatlib.transfer.ItemStorage;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageUtil;
import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.ResourceAmount;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings("UnstableApiUsage")
public class ProcessInterfaceBlockEntity extends SyncableBlockEntity implements InterruptEmitter, RoutablePipe, MemoryProvider
{
    public static final int MAX_CARDS = 18;
    private final Set<InterruptReceiver> receivers = new HashSet<>();

    // IMPORTANT: amount is number of batches, not requested amount
    private final ObjectArrayList<ResourceAmount<ItemVariant>> requests = new ObjectArrayList<>();

    private final List<BOMEntry> bom = new ObjectArrayList<>();

    private final ImplementedInventory returnInventory = ImplementedInventory.ofSize(9, this::setChanged);
    private final InventoryStorage returnStorage = InventoryStorage.of(returnInventory, null);
    private final BackStorageDelegate backStorageDelegate = new BackStorageDelegate();
    @Nullable private List<ProcessDef> processDefs = null;
    private final ImplementedInventory cardInventory = ImplementedInventory.ofSize(18, () -> processDefs = null);

    private final LazyBlockApiCache<Storage<ItemVariant>, Direction> cache = DynamicLazyBlockApiCache.facing(ItemStorage.SIDED, this, () ->
            getBlockState().getValue(ProcessInterfaceBlock.FACING));

    public boolean useInterrupts = false;
    public boolean delayInterrupts = true;
    public boolean recordQueue = false;

    public ProcessInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void addListener(InterruptReceiver receiver)
    {
        receivers.add(receiver);
    }

    private List<ProcessDef> getProcessDefs()
    {
        if (processDefs == null)
        {
            processDefs = new ObjectArrayList<>();
            for (var card : cardInventory.getItems())
            {
                var component = NMComponents.PROCESS_STORAGE.getNullable(card);
                if (component != null && !component.getResult().resource().isBlank())
                {
                    processDefs.add(new ProcessDef(
                            component.getItems().getItems().stream().filter(s -> !s.isEmpty())
                                    .map(s -> new ResourceAmount<>(ItemVariant.of(s), s.getCount())).toList(),
                            component.getResult()));
                }
            }
        }

        return processDefs;
    }

    @Override
    public Result request(Predicate<ItemVariant> predicate, long amount, NodePos fromPos, RoutingNetwork.RequestContext context, TransactionContext transaction)
    {
        long immediateAmount = 0;
        long deferredAmount = 0;

        Direction facing = getBlockState().getValue(ProcessInterfaceBlock.FACING);

        // Check the immediately available results
        var extractable = StorageUtil.findExtractableContent(returnStorage, predicate, transaction);
        if (extractable != null && extractable.amount() > 0)
        {
            immediateAmount = Math.min(extractable.amount(), amount);
        }

        ItemVariant deferredVariant = null;

        Map<ItemVariant, Long> residuals = new HashMap<>();

        // Look up the process definition, request the ingredients and submit a deferred request
        if (immediateAmount < amount)
        {
            for (ProcessDef process : getProcessDefs())
            {
                if (predicate.test(process.result.resource()))
                {
                    deferredVariant = process.result.resource();

                    // Craft enough or more than enough - ceil
                    long crafts = (int) Math.ceil((amount - immediateAmount) / (double) process.result.amount());
                    BlockPos requestPos = worldPosition.relative(facing.getOpposite());
                    if (level.getBlockEntity(requestPos) instanceof ItemPipeBlockEntity ipbe)
                    {
                        for (ResourceAmount<ItemVariant> dependency : process.dependencies)
                        {
                            long requiredDependencyAmount = crafts * dependency.amount();

                            // Request the dependency
                            var network = ipbe.getCache().getNetwork();
                            if (network != null)
                            {
                                if (!network.request(v -> v.getItem() == dependency.resource().getItem(), requiredDependencyAmount,
                                        requestPos, facing, RoutingNetwork.RequestType.EXACT_AMOUNT, context, transaction))
                                    return Result.FAIL;

                                residuals.put(dependency.resource(), requiredDependencyAmount);
                            }
                        }
                    }

                    deferredAmount = amount - immediateAmount;

                    // Skip later compatible process cards
                    break;
                }
            }
        }

        // ToDO
//        RoutingOperation operation = null;

//        if (deferredAmount > 0 && deferredVariant == null) // Not available here
//        {
//            return Result.immediate(t -> {}, 0);
//        }
//        else if (deferredAmount > 0)
//        {
//            bom.add(new BOMEntry(deferredVariant, residuals));
//            return new Result(operation, immediateAmount, deferredAmount, deferredVariant);
//        }
//        else
//        {
//            return Result.immediate(operation, immediateAmount);
//        }
        return null;
    }

    @Override
    public long request2(Predicate<ItemVariant> predicate, long amount, NodePos dest, TransactionContext transaction)
    {
        long remaining = amount;
        ItemVariant extractable = null;
        try (Transaction inner = transaction.openNested())
        {
            for (var view : returnStorage)
            {
                ItemVariant resource = view.getResource();
                if (predicate.test(resource))
                {
                    long toExtract = Math.min(view.getAmount(), remaining);

                    // We can do this because the views are normal
                    view.extract(resource, toExtract, inner);
                    extractable = resource;
                    remaining -= toExtract;

                    if (remaining <= 0)
                        break;
                }
            }

            if (remaining == 0 && extractable != null)
            {
                inner.commit();

                PipeCacheImpl itemNetwork = ((IServerWorld) level).getItemNetwork();
                var facing = getBlockState().getValue(ProcessInterfaceBlock.FACING);

                // Check storage before finding a route
                ItemRoute route = itemNetwork.findPath(
                        new ResourceAmount<>(extractable, amount),
                        worldPosition.relative(facing.getOpposite()), dest.pos(), dest.face());
                long routed = itemNetwork.route(worldPosition, facing.getOpposite(), route, extractable, amount, transaction);

                if (routed < amount)
                    MeatlibStorageUtil.scatterAmount(level, worldPosition, new ResourceAmount<>(extractable, amount - routed));

                return routed;
            }
        }

        return 0;
    }

    @Override
    public DeferredResult deferredUpdate(RoutingNetwork.DeferredRequest request, RoutingNetwork network, TransactionContext transaction)
    {
        ItemVariant extractable = request.getItemVariant();
        long amount = request.getRequested();

        if (amount == 0)
            return DeferredResult.SUCCESS;

        long remaining = amount;
        try (Transaction inner = transaction.openNested())
        {
            for (var view : returnStorage)
            {
                ItemVariant resource = view.getResource();
                if (request.getItemVariant().equals(resource))
                {
                    long toExtract = Math.min(view.getAmount(), remaining);

                    // We can do this because the views are normal
                    view.extract(resource, toExtract, inner);
                    remaining -= toExtract;

                    if (remaining <= 0)
                        break;
                    ;
                }
            }

            if (remaining == 0)
            {
                inner.commit();

                PipeCacheImpl itemNetwork = ((IServerWorld) level).getItemNetwork();
                var facing = getBlockState().getValue(ProcessInterfaceBlock.FACING);

                // Check storage before finding a route
                ItemRoute route = itemNetwork.findPath(
                        new ResourceAmount<>(extractable, amount),
                        worldPosition.relative(facing.getOpposite()), request.requesterPos, request.requesterFace);
                long routed = itemNetwork.route(worldPosition, facing.getOpposite(), route, extractable, amount, transaction);

                if (routed < amount)
                    MeatlibStorageUtil.scatterAmount(level, worldPosition, new ResourceAmount<>(extractable, amount - routed));

                return DeferredResult.SUCCESS;
            }
        }

        return DeferredResult.PASS;
    }

    @Override
    public void notifyCraft(ResourceAmount<ItemVariant> result, List<Dependency> dependencies, long batches)
    {
        if (useInterrupts)
        {
            bom.add(BOMEntry.of(result.resource(), dependencies, batches));
            setChanged();
        }
    }

    @Override
    public @Nullable NodePos requestDestPosDir()
    {
        var facing = getBlockState().getValue(ProcessInterfaceBlock.FACING);
        return new NodePos(worldPosition.relative(facing.getOpposite()), facing);
    }

    @Override
    public Stream<StorageView<ItemVariant>> getAvailable(TransactionContext transaction)
    {
        return StreamSupport.stream(returnStorage.spliterator(), false);
    }

    @Override
    public Stream<Craftable> getCraftable(TransactionContext transaction)
    {
        return getProcessDefs().stream().map(p ->
                new Craftable()
                {
                    @Override
                    public ResourceAmount<ItemVariant> result()
                    {
                        return p.result;
                    }

                    @Override
                    public Dependencies getDependencies(long amount, TransactionContext transaction)
                    {
                        int batchSize = Mth.positiveCeilDiv((int) amount, (int) p.result.amount());
                        int extra = Math.toIntExact(amount - batchSize * p.result.amount());
                        return new Dependencies(p.dependencies.stream().map(ra -> new Dependency(
                                CraftingPredicate.equals(ra.resource()), ra.amount() * batchSize)).toList(),
                                extra, batchSize);
                    }

                    @Override
                    public RoutablePipe underlying()
                    {
                        return ProcessInterfaceBlockEntity.this;
                    }
                });
    }

    @Override
    public BlockPos getRoutablePipePos()
    {
        return worldPosition;
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        requests.clear();
        ListTag list = nbt.getList("list", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i)
        {
            CompoundTag compound = list.getCompound(i);

            requests.add(PlcInstruction.readItem(compound));
        }

        returnInventory.readNbt(nbt.getCompound("results"));

        cardInventory.readNbt(nbt.getCompound("cards"));
        useInterrupts = nbt.getBoolean("use_interrupts");
        delayInterrupts = nbt.getBoolean("delay_interrupts");
        recordQueue = nbt.getBoolean("record_queue");

        if (useInterrupts)
        {
            bom.clear();
            nbt.getList("bom", Tag.TAG_COMPOUND).forEach(e ->
                    bom.add(BOMEntry.fromNbt((CompoundTag) e)));
        }

        if (recordQueue)
        {
            requests.clear();
            nbt.getList("requests", Tag.TAG_COMPOUND).forEach(e ->
                    requests.add(MeatlibStorageUtil.amountFromNbt((CompoundTag) e)));
        }
    }


    @Override
    public void saveToItem(ItemStack stack)
    {
        CompoundTag createdNbt = saveWithoutMetadata();
        createdNbt.remove("results"); // Do not save results
        BlockItem.setBlockEntityData(stack, getType(), createdNbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        ListTag list = new ListTag();
        requests.forEach(r ->
        {
            list.add(PlcInstruction.writeItem(r));
        });

        nbt.put("list", list);

        nbt.put("results", returnInventory.writeNbt(new CompoundTag()));
        nbt.put("cards", cardInventory.writeNbt(new CompoundTag()));
        nbt.putBoolean("use_interrupts", useInterrupts);
        nbt.putBoolean("delay_interrupts", delayInterrupts);
        nbt.putBoolean("record_queue", recordQueue);

        if (useInterrupts)
        {
            nbt.put("bom", bom.stream().collect(ListTag::new, (e, a) ->
                    e.add(a.toNbt()), AbstractCollection::addAll));
        }

        if (recordQueue)
        {
            nbt.put("requests", requests.stream().collect(ListTag::new,
                    (e, a) -> e.add(MeatlibStorageUtil.itemAmountToNbt(a)),
                    AbstractCollection::addAll));
        }
    }

    @Override
    public @Nullable MemoryEntry[] poll()
    {
        if (!requests.isEmpty())
        {
            var request = requests.get(requests.size() - 1);

            requests.remove(requests.size() - 1);

            return new MemoryEntry[]{
                    new MemoryEntry.StringEntry(BuiltInRegistries.ITEM.getKey(request.resource().getItem()).toString()),
                    new MemoryEntry.IntegerEntry((int) request.amount())};
        }

        return new MemoryEntry[]{
                new MemoryEntry.StringEntry(""),
                new MemoryEntry.IntegerEntry(-1)};
    }

    @Override
    public void push(MemoryEntry entry)
    {

    }

    public Storage<ItemVariant> getStorage(Direction face)
    {
        if (face == getBlockState().getValue(ProcessInterfaceBlock.FACING).getOpposite())
        {
            return backStorageDelegate;
        }
        else
        {
            return returnStorage;
        }
    }

    public Container getResult()
    {
        return returnInventory;
    }

    public ObjectArrayList<ResourceAmount<ItemVariant>> getRequests()
    {
        return requests;
    }

    public ImplementedInventory getCardInventory()
    {
        return cardInventory;
    }

    public void setUseInterrupts(boolean useInterrupts, boolean delayInterrupts, boolean recordQueue)
    {
        this.useInterrupts = useInterrupts;
        this.delayInterrupts = delayInterrupts;
        this.recordQueue = recordQueue;
        setChanged();
    }

    record ProcessDef(List<ResourceAmount<ItemVariant>> dependencies, ResourceAmount<ItemVariant> result)
    {
    }

    // Used to detect whether a request's ingredients have arrived
    static class BOMEntry
    {
        private final ItemVariant result;
        private final List<MutableDependency> dependencies;
        private final long batches;

        public static BOMEntry of(ItemVariant result, List<Dependency> dependencies, long batches)
        {
            return new BOMEntry(result, dependencies.stream().map(d -> new MutableDependency(d.predicate(), d.amount()))
                    .collect(ObjectArrayList::new, ObjectArrayList::add, ObjectArrayList::addAll), batches);
        }

        private BOMEntry(ItemVariant result, List<MutableDependency> dependencies, long batches)
        {
            this.result = result;
            this.dependencies = dependencies;
            this.batches = batches;
        }

        public long tickOff(ItemVariant variant, long amount)
        {
            for (Iterator<MutableDependency> iterator = dependencies.iterator(); iterator.hasNext(); )
            {
                var dependency = iterator.next();
                if (dependency.predicate.test(variant))
                {
                    long newValue = Math.max(0, dependency.amount - amount);
                    long decrement = dependency.amount - newValue;
                    dependency.amount -= decrement;

                    if (dependency.amount <= 0)
                    {
                        iterator.remove();
                    }

                    return decrement;
                }
            }

            return 0;
        }

        public boolean isDone()
        {
            return dependencies.isEmpty();
        }

        public Tag toNbt()
        {
            CompoundTag nbt = new CompoundTag();
            nbt.put("result", result.toNbt());
            nbt.put("dependencies", dependencies.stream().map(MutableDependency::toNbt).collect(ListTag::new, AbstractCollection::add, AbstractCollection::addAll));
            nbt.putLong("batches", batches);
            return nbt;
        }

        public static BOMEntry fromNbt(CompoundTag nbt)
        {
            return new BOMEntry(
                    ItemVariant.fromNbt(nbt.getCompound("result")),
                    nbt.getList("dependencies", Tag.TAG_COMPOUND)
                            .stream()
                            .map(e -> MutableDependency.fromNbt((CompoundTag) e))
                            .collect(ObjectArrayList::new, AbstractCollection::add, AbstractCollection::addAll),
                    nbt.getLong("batches")
                    );
        }

        private static class MutableDependency
        {
            private final Predicate<ItemVariant> predicate;
            private long amount;

            public MutableDependency(Predicate<ItemVariant> predicate, long initialAmount)
            {
                 this.predicate = predicate;
                 this.amount = initialAmount;
            }

            public CompoundTag toNbt()
            {
                var nbt = new CompoundTag();
                nbt.put("predicate", ((CraftingPredicate) predicate).writeNbt(new CompoundTag()));
                nbt.putLong("amount", amount);
                return nbt;
            }

            public static MutableDependency fromNbt(CompoundTag nbt)
            {
                return new MutableDependency(
                        CraftingPredicate.fromNbt(nbt.getCompound("predicate")),
                        nbt.getLong("amount"));
            }
        }
    }

    private class BackStorageDelegate implements Storage<ItemVariant>
    {
        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction)
        {
            // Tick off the BOM entries
            long remaining = maxAmount;

            for (var it = bom.iterator(); it.hasNext(); )
            {
                var req = it.next();
                remaining -= req.tickOff(resource, maxAmount);

                if (req.isDone())
                {
                    it.remove();
                    addInterrupt(req);
//                    PlayerLookup.around((ServerWorld) world, pos, 10)
//                            .forEach(p -> p.sendMessage(Text.literal("Result: " + req.result.getItem().getName().getString())));
                }

                if (remaining <= 0)
                    break;
            }

            var storage = cache.find();
            if (storage != null)
            {
                return storage.insert(resource, maxAmount, transaction);
            }
            else
            {
                return 0;
            }
        }

        private void addInterrupt(BOMEntry req)
        {
            for (InterruptReceiver receiver : receivers)
                receiver.interrupt(worldPosition, ProcessInterfaceBlockEntity.this);

            if (recordQueue)
            {
                requests.add(new ResourceAmount<>(req.result, req.batches));
                setChanged();
            }
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction)
        {
            return 0;
        }

        @Override
        public Iterator<StorageView<ItemVariant>> iterator()
        {
            return Collections.emptyIterator();
        }
    }
}
