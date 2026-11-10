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

package com.neep.neepmeat.transport.block.energy_transport.entity;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepbus.block.entity.ConfigProvider;
import com.neep.neepbus.util.*;
import com.neep.neepbus.util.message.DataVariant;
import com.neep.neepmeat.transport.api.pipe.BalancingBloodAcceptor;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class VascularSensorBlockEntity extends SyncableBlockEntity implements ConfigProvider
{
    private final BloodAcceptor acceptor = new Acceptor();

    private long networkInternal;

    private final CachingSender sender = new CachingSender(this::getLevel, this.worldPosition);
    private final DirectReadPort capacityRead = new DirectReadPort(new SimpleEntry("Network capacity", "On data read:\nReturn the capacity of the connected vascular network."), () -> DataVariant.of(networkInternal), sender::send);

    private final NeepBusConfig config = NeepBusConfig.builder(this::setChanged)
            .output(capacityRead.entry(), capacityRead)
            .build();

    public VascularSensorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public BloodAcceptor getBloodAcceptor(Direction direction)
    {
        return acceptor;
    }

    @Override
    public NeepBusConfig getConfig()
    {
        return config;
    }

    public void invalidateSender()
    {
        sender.invalidate();
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        config.writeNbt(nbt);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        config.readNbt(nbt);
    }

    private class Acceptor implements BloodAcceptor, BalancingBloodAcceptor
    {
        public Mode getMode()
        {
            return Mode.BALANCE;
        }

        @Override
        public long update(long internalAbs)
        {
            if (networkInternal != internalAbs)
            {
                networkInternal = internalAbs;
                capacityRead.send();
            }
            return 0;
        }

        @Override
        public int priority()
        {
            return 0;
        }
    }
}
