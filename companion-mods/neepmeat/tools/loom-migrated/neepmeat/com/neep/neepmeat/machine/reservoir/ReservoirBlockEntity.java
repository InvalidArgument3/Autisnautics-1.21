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

package com.neep.neepmeat.machine.reservoir;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.FluidPump;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.transport.fluid_network.node.AcceptorModes;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class ReservoirBlockEntity extends SyncableBlockEntity
{
    private final WritableSingleFluidStorage storage;
    private final FluidPump pump = FluidPump.of(-1, () -> AcceptorModes.PUSH);

    private long placedTime;

    public ReservoirBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        storage = new WritableSingleFluidStorage(8 * FluidConstants.BUCKET, this::setChanged)
        {
            @Override
            protected boolean canInsert(FluidVariant variant)
            {
                return variant.isOf(Fluids.WATER);
            }
        };
    }

    public @Nullable Storage<FluidVariant> getStorage(@Nullable Direction direction)
    {
        return storage;
    }

    public @Nullable FluidPump getPump(Direction direction)
    {
        return pump;
    }

    public void onPlaced(Level world)
    {
        placedTime = world.getGameTime();
    }

    public void serverTick()
    {
        if (level.getGameTime() % 20 == 0)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                storage.insert(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET, transaction);
                transaction.commit();
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putLong("placed_time", placedTime);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.placedTime = nbt.getLong("placed_time");
    }
}
