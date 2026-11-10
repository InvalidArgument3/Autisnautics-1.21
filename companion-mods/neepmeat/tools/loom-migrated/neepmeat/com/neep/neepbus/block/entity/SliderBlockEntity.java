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

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepbus.part.Slider;
import com.neep.neepbus.util.*;
import com.neep.neepbus.util.message.DataVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SliderBlockEntity extends SyncableBlockEntity implements Slider, ConfigProvider
{
    private final CachingSender sender = new CachingSender(this::getLevel, getBlockPos());

    private final DirectReadPort outputPort = new DirectReadPort(
            new SimpleEntry("Output"),
            () -> DataVariant.of(this.getValue()),
            sender::send);

    private final NeepBusConfig config = NeepBusConfig.builder(this::sync)
            .output(outputPort.entry(), outputPort)
            .applyChanges(this)
            .build();

    private float value = 0;
    private float minValue = 0;
    private float maxValue = 256;
    private float interval = 16;

    public SliderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public float getValue()
    {
        return value;
    }

    @Override
    public float getMinValue()
    {
        return minValue;
    }

    @Override
    public float getMaxValue()
    {
        return maxValue;
    }

    @Override
    public void setValues(float value, float minValue, float maxValue)
    {
        this.value = Mth.clamp(value, minValue, maxValue);
        this.minValue = Math.min(minValue, maxValue);
        this.maxValue = Math.max(maxValue, minValue);
        sync();
    }

    @Override
    public float getInterval()
    {
        return interval;
    }

    @Override
    public void setInterval(float interval)
    {
        this.interval = Mth.clamp(interval, 0.001f, maxValue - minValue);
        sync();
    }

    public void updateNetwork()
    {
        sender.invalidate();
    }

    @Override
    public NeepBusConfig getConfig()
    {
        return config;
    }

    @Override
    public void increment(double amount, boolean large)
    {
        float increment = (Math.signum((float) amount) * Math.max(
                (large ? interval : interval / 10f),
                0.001f));
        value = Mth.clamp(value + increment, minValue, maxValue);
        outputPort.send();
        sync();
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putFloat("value", value);
        nbt.putFloat("min_value", minValue);
        nbt.putFloat("max_value", maxValue);
        nbt.putFloat("divisions", interval);

        nbt.put("config", config.writeNbt(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.value = nbt.getFloat("value");
        this.minValue = nbt.getFloat("min_value");
        this.maxValue = nbt.getFloat("max_value");
        this.interval = nbt.getFloat("divisions");

        this.config.readNbt(nbt.getCompound("config"));
    }

    public void invalidate()
    {
        sender.invalidate();
    }
}
