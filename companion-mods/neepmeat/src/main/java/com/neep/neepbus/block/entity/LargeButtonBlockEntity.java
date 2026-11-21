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

public class LargeButtonBlockEntity extends SyncableBlockEntity implements ConfigProvider, Button
{
    private DataVariant highValue = DataVariant.of(1);
    private DataVariant value = DataVariant.NULL;
    private int fallDelay = 0;

    private final CachingSender sender = new CachingSender(this::getLevel, worldPosition);

    private final ConfigEntry outputEntry = new SimpleEntry("Button output");
    private final DirectReadPort output = new DirectReadPort(outputEntry, () -> value, sender::send);
    private final NeepBusConfig config = NeepBusConfig.builder(this::sync)
            .output(outputEntry, output)
            .build();

    public LargeButtonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public void invalidate()
    {
        sender.invalidate();
    }

    @Override
    public NeepBusConfig getConfig()
    {
        return config;
    }

    public void trigger()
    {
        value = highValue;
        output.send();

        if (fallDelay > 0)
        {
            level.scheduleTick(worldPosition, getBlockState().getBlock(), fallDelay);
        }
    }

    public void onScheduledTick()
    {
        value = DataVariant.NULL;
        output.send();
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        config.writeNbt(nbt);
        nbt.putString("current_value", value.stringValue());
        nbt.putString("value", highValue.stringValue());
        nbt.putInt("fall_delay", fallDelay);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        config.readNbt(nbt);
        this.value = DataVariant.of(nbt.getString("current_value"));
        this.highValue = DataVariant.of(nbt.getString("value"));
        this.fallDelay = nbt.getInt("fall_delay");
    }

    @Override
    public void setValues(DataVariant value, int fallDelay)
    {
        this.highValue = value;
        this.fallDelay = fallDelay;
        setChanged();
    }

    @Override
    public DataVariant getHighValue()
    {
        return highValue;
    }

    @Override
    public int getFallDelay()
    {
        return fallDelay;
    }
}
