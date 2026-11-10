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

package com.neep.neepmeat.machine.bottler;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.block.BaseHorFacingBlock;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import com.neep.neepmeat.block.entity.MotorisedMachineBlockEntity;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.machine.motor.MotorEntity;
import com.neep.neepmeat.transport.util.ItemPipeUtil;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageUtil;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("UnstableApiUsage")
public class BottlerBlockEntity extends MotorisedMachineBlockEntity
{
    private final BottlerStorage storage = new BottlerStorage(this);
    public static final int MAX_PROGRESS = 5;
    private float progress;
    private int maxProgress = MAX_PROGRESS;
    private long startTime;

    private State state = State.IDLE;
    private float powdder = 0;

    public BottlerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state, 0.02f, 1f);
    }

    public BottlerBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.BOTTLER, pos, state);
    }

    public WritableStackStorage getItemStorage(Direction direction)
    {
        return storage.getItemStorage();
    }

    public Storage<FluidVariant> getStorage(Direction direction)
    {
        return storage.getFluidStorage();
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        storage.writeNbt(nbt);
        nbt.putFloat("progress", progress);
        nbt.putInt("maxProgress", maxProgress);
        nbt.putLong("startTime", startTime);
        nbt.putInt("state", state.ordinal());
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        storage.readNbt(nbt);
        this.progress = nbt.getFloat("progress");
        this.maxProgress = nbt.getInt("maxProgress");
        this.startTime = nbt.getLong("startTime");
        this.state = State.values()[nbt.getInt("state")];
    }

    private Storage<FluidVariant> getInputStorage()
    {
        return FluidStorage.SIDED.find(level, worldPosition.below(), Direction.UP);
    }

    @Override
    public boolean motorTick(MotorEntity motor)
    {
        try (Transaction transaction = Transaction.openOuter())
        {
            switch (state)
            {
                case IDLE:
                {
                    {
                        tryStart(transaction);
                        transaction.commit();
                    }
                    break;
                }

                case FILLING:
                {
                    // Stop if item is removed
                    if (storage.getItemStorage().isEmpty()) interrupt();

                    // Increment progress
                    this.progress = Math.min(maxProgress, progress + progressIncrement);
                    if (progress == maxProgress)
                    {
                        progress = 0;
                        succeed(transaction);
                        transaction.commit();
                    }
                    return true;
                }

                case EJECTING:
                {
                    Direction facing = getBlockState().getValue(BaseHorFacingBlock.FACING);
                    if (storage.getItemStorage().isEmpty() || ItemPipeUtil.storageToAny((ServerLevel) level, storage.getItemStorage(), worldPosition, facing, transaction))
                    {
                        state = State.IDLE;
                    }
                    transaction.commit();
                    return true;
                }
            }
        }
        return false;
    }

    void interrupt()
    {
        state = State.IDLE;
        progress = 0;
        sync();
    }

    protected void tryStart(TransactionContext transaction)
    {
        try (Transaction inner = transaction.openNested())
        {
            if (moveToItem(inner) > 0)
            {
                state = State.FILLING;
                maxProgress = MAX_PROGRESS;
                startTime = level.getGameTime();
                sync();
                inner.abort();
            }
        }
    }

    protected void succeed(TransactionContext transaction)
    {
        moveToItem(transaction);
        state = State.EJECTING;
        sync();
    }

    private long moveToItem(TransactionContext transaction)
    {
        Storage<FluidVariant> inputStorage = getInputStorage();
        Storage<FluidVariant> outputStorage = storage.getFluidStorage();
        if (inputStorage != null && outputStorage != TransferApiImpl.EMPTY_STORAGE)
        {
            return StorageUtil.move(inputStorage, outputStorage, v -> true, Long.MAX_VALUE, transaction);
        }
        return 0;
    }

    public int getMaxProgress()
    {
        return maxProgress;
    }

    public long getStartTime()
    {
        return startTime;
    }

    private enum State
    {
        IDLE,
        FILLING,
        EJECTING
    }
}