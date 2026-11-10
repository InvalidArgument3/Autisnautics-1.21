package com.neep.neepmeat.transport.block.item_transport.entity;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.storage.DynamicLazyBlockApiCache;
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
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
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.put("storage", storage.toNbt(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.storage.readNbt(nbt.getCompound("storage"));
    }
}
