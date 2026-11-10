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

package com.neep.neepmeat.machine.dumper;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import com.neep.neepmeat.init.NMBlockEntities;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class DumperBlockEntity extends SyncableBlockEntity
{
    protected WritableStackStorage storage;
    protected long lastDrop;
    protected int interval = 10;
    protected int counter;
    private boolean active = true;

    public DumperBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        storage = new WritableStackStorage(this::setChanged, 64);
    }

    public DumperBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.DUMPER, pos, state);
    }

    public WritableStackStorage getStorage(@Nullable Direction direction)
    {
        return direction != Direction.DOWN ? storage : null;
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        storage.writeNbt(nbt);
        nbt.putInt("counter", counter);
        nbt.putBoolean("active", active);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        storage.readNbt(nbt);
        this.counter = nbt.getInt("counter");
        this.active = nbt.getBoolean("active");
    }

    public static void dropItem(ServerLevel world, BlockPos pos, ItemVariant variant)
    {
        double x = pos.getX() + 0.5;
        double y = pos.getY();
        double z = pos.getZ() + 0.5;
        ItemEntity item = new ItemEntity(world, x, y, z, variant.toStack(1), 0, -0.1, 0);
        world.addFreshEntity(item);
    }

    // Returns true if the item storage below can accept items or if there is no item storage below.
    public static boolean canDropItem(ServerLevel world, BlockPos startPos, ItemVariant resource, long amount, TransactionContext transaction)
    {
        int distance = 4;
        BlockPos pos = startPos;
        for (int i = 0; i < distance; ++i)
        {
            pos = pos.relative(Direction.DOWN);
            if (!world.isEmptyBlock(pos))
                break;
        }

        Storage<ItemVariant> storage = ItemStorage.SIDED.find(world, pos, Direction.UP);
        return storage == null || MeatlibStorageUtil.simulateInsert(storage, resource, amount, transaction) == amount;
    }

    public void tick()
    {

        counter = Math.min(interval, counter + 1);

        if (counter >= interval && active)
        {
            counter = 0;

            lastDrop = level.getGameTime();
            try (Transaction transaction = Transaction.openOuter())
            {
                long extractAmount = 1;
                ItemVariant resource = storage.getResource();
                if (!resource.isBlank()
                        && canDropItem((ServerLevel) level, worldPosition, resource, extractAmount, transaction)
                        && storage.extract(resource, extractAmount, transaction) == extractAmount)
                {
                    dropItem((ServerLevel) level, worldPosition, resource);
                    transaction.commit();
                }
                else transaction.abort();
            }
        }
    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, DumperBlockEntity be)
    {
        be.tick();
    }

    public void updateActive(boolean power)
    {
        this.active = power;
    }
}
