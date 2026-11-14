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

import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.neepmeat.transport.block.FlexStorageBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.FlexSiloBlock;
import com.neep.meatlib.transfer.ItemStorage;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.ResourceAmount;
import com.neep.meatlib.transfer.SingleVariantStorage;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;

public class FlexSiloBlockEntity extends FlexStorageBlockEntity<FlexSiloBlockEntity>
{
    private final StorageCache<ItemVariant> cache = new StorageCache<>(ItemStorage.SIDED, this);
    private final FlexSiloStorage storage;
    private final FlexSiloBlock.Capacity capacity;

    public FlexSiloBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, FlexSiloBlock.Capacity capacity)
    {
        super(type, pos, state);
        this.capacity = capacity;
        this.storage = new FlexSiloStorage();
    }

    public Storage<ItemVariant> getStorage(Direction ignored)
    {
        return storage;
    }

    @Override
    protected StorageDelegate<FlexSiloBlockEntity> getStorageDelegate()
    {
        return storage;
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        storage.writeNbt(nbt);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        storage.readNbt(nbt);
    }

    public void dumpItems()
    {
        MeatlibStorageUtil.scatter(level, worldPosition, storage);
    }

    /// Stores a number of stacks of configurable size.
    /// Delegates to the root storage if not root.
    private class FlexSiloStorage extends BogItemStorage implements StorageDelegate<FlexSiloBlockEntity>
    {
        public FlexSiloStorage()
        {
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction)
        {
            if (isRoot())
            {
                return super.insert(resource, maxAmount, transaction);
            }
            else if (root != null)
            {
                return cache.find().insert(resource, maxAmount, transaction);
            }
            return 0;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction)
        {
            if (isRoot())
            {
                return super.extract(resource, maxAmount, transaction);
            }
            else if (root != null)
            {
                return cache.find().extract(resource, maxAmount, transaction);
            }
            return 0;
        }

        @Override
        public @NotNull Iterator<StorageView<ItemVariant>> iterator()
        {
            if (isRoot())
            {
                return super.iterator();
            }
            else if (root != null)
            {
                return cache.find().iterator();
            }
            return Collections.emptyIterator();
        }

        @Override
        protected int getStackCapacity()
        {
            return children.size() * capacity.types();
        }

        @Override
        protected long getStackSize()
        {
            return capacity.stackSize();
        }

        @Override
        public void validate()
        {
            // TODO
            updateLists(v ->
            {
                MeatlibStorageUtil.scatterAmount(level, worldPosition, new ResourceAmount<>(v.getResource(), v.getAmount()));
            });
        }

        @Override
        public boolean isEmpty()
        {
            return viewList.stream().allMatch(SingleVariantStorage::isResourceBlank);
        }

        @Override
        public void transferInto(FlexSiloBlockEntity root, TransactionContext transaction)
        {
            for (var view : nonEmptyViews())
            {
                var resource = view.getResource();
                long toInsert = view.getAmount();
                view.extract(resource, toInsert, transaction);
                toInsert -= root.storage.insert(resource, toInsert, transaction);
                if (toInsert > 0)
                {
                    MeatlibStorageUtil.scatterAmount(getLevel(), worldPosition, new ResourceAmount<>(resource, toInsert));
                }
            }
        }
    }
}
