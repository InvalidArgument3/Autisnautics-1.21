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

package com.neep.neepbus.block.entity;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepbus.util.*;
import com.neep.neepbus.util.message.DataVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class UnitDelayBlockEntity extends SyncableBlockEntity implements ConfigProvider, Delay
{
    private DataVariant data = DataVariant.NULL;
    private DataVariant prevData = DataVariant.NULL;
    private int delay;

    private final CachingSender sender = new CachingSender(this::getLevel, getBlockPos());

    private final DirectReadPort output = new DirectReadPort(new SimpleEntry("Delay output"), () -> prevData, sender::send);
    private final WritePort input = this::receive;

    private final NeepBusConfig config = NeepBusConfig.builder(this::sync)
            .applyChanges(this)
            .input(new SimpleEntry("Input"), input)
            .output(output.entry(), output)
            .build();

    public UnitDelayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    private void receive(DataVariant data)
    {
        this.data = data;
        level.scheduleTick(worldPosition, this.getBlockState().getBlock(), delay);
        setChanged();
    }

    public void onScheduledTick()
    {
        prevData = data;
        output.send();
    }

    @Override
    public NeepBusConfig getConfig()
    {
        return config;
    }

    public void invalidate()
    {
        sender.invalidate();
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        nbt.putString("data", data.stringValue());
        nbt.putString("prevData", prevData.stringValue());
        nbt.putInt("delay", delay);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.data = DataVariant.of(nbt.getString("data"));
        this.prevData = DataVariant.of(nbt.getString("prevData"));
        this.delay = Math.max(1, nbt.getInt("delay"));
    }

    @Override
    public int getDelay()
    {
        return delay;
    }

    @Override
    public void setDelay(int delay)
    {
        this.delay = Math.max(1, delay);
    }
}
