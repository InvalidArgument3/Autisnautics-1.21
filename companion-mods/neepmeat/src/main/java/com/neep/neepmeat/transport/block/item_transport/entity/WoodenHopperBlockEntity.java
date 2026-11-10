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

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.storage.DynamicLazyBlockApiCache;
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageUtil;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class WoodenHopperBlockEntity extends SyncableBlockEntity
{
    protected final WritableStackStorage storage = new WritableStackStorage(this::setChanged);

    private int transferCooldown = 0;

    private final LazyBlockApiCache<Storage<ItemVariant>, Direction> cache = addListener(DynamicLazyBlockApiCache.facing(
            ItemStorage.SIDED, this, () -> getBlockState().getValue(HopperBlock.FACING)));
    private final LazyBlockApiCache<Storage<ItemVariant>, Direction> upCache = addListener(LazyBlockApiCache.of(
            ItemStorage.SIDED, worldPosition.relative(Direction.UP), this::getLevel, () -> Direction.DOWN));

    public WoodenHopperBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Nullable
    public Storage<ItemVariant> getStorage(@Nullable Direction direction)
    {
        return storage;
    }

    public void serverTick()
    {
        tryUpdateComparators();
        transferCooldown--;

        if (transferCooldown <= 0)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                pushSide(transaction);
                pullDown(transaction);

                transaction.commit();
            }


            transferCooldown = 8;
        }
    }

    protected void pushSide(TransactionContext transaction)
    {
        var found = cache.find();
        if (found != null)
        {
            StorageUtil.move(storage, found, v -> true, 1, transaction);
        }
    }

    protected void pullDown(TransactionContext transaction)
    {
        var found = upCache.find();

        if (found != null)
        {
            StorageUtil.move(found, storage, v -> true, 1, transaction);
        }
    }

    public void onEntityCollided(ItemEntity itemEntity)
    {
        try (Transaction transaction = Transaction.openOuter())
        {
            ItemStack stack = itemEntity.getItem();
            if (stack.isEmpty())
                return;

            long inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
            stack.shrink((int) inserted);
            if (stack.isEmpty())
            {
                itemEntity.remove(Entity.RemovalReason.DISCARDED);
                transaction.commit();
                return;
            }
            itemEntity.setItem(stack);
            transaction.commit();
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        nbt.put("storage", storage.toNbt(new CompoundTag()));
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.storage.readNbt(nbt.getCompound("storage"));
    }
}
