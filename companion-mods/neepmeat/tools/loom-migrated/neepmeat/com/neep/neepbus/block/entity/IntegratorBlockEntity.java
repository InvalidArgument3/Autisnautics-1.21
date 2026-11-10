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
import com.neep.neepbus.util.*;
import com.neep.neepbus.util.message.DataVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class IntegratorBlockEntity extends SyncableBlockEntity implements ConfigProvider, SignalIntegrator
{
    private final CachingSender sender = new CachingSender(this::getLevel, worldPosition);

    private final ConfigEntry inputEntry = new SimpleEntry("Input");
    private final ConfigEntry clearEntry = new SimpleEntry("Clear");

    private final WritePort input = this::receive;
    private final WritePort clear = this::clear;

    private final DirectReadPort output = new DirectReadPort(new SimpleEntry("Integral"), () -> DataVariant.of(this.integral), sender::send);

    private final NeepBusConfig config = NeepBusConfig.builder(this::sync)
            .input(inputEntry, input)
            .input(clearEntry, clear)
            .output(output.entry(), output)
            .applyChanges(this)
            .build();

    private float integral;

    private int interval = 5;
    private float timeFactor = (float) 1;
    private float inputValue;

    public IntegratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    private void receive(DataVariant data)
    {
        inputValue = data.floatValue();
    }

    @Override
    public NeepBusConfig getConfig()
    {
        return config;
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        config.writeNbt(nbt);
        nbt.putFloat("integral", integral);
        nbt.putInt("interval", interval);
        nbt.putFloat("time_factor", timeFactor);
        nbt.putFloat("input", inputValue);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        config.readNbt(nbt);
        this.integral = nbt.getFloat("integral");
        this.interval = nbt.getInt("interval");
        this.timeFactor = nbt.getFloat("time_factor");
        this.inputValue = nbt.getFloat("input");
    }

    public void invalidate()
    {
        sender.invalidate();
    }

    public void serverTick()
    {
        if (interval > 0 && level.getGameTime() % interval == 0)
        {
//            integral += (sender.read(inputEntry.getAddress()) * (float) interval * timeFactor);
            integral += inputValue * (float) interval * timeFactor;
            output.send();
        }
    }

    @Override
    public void setValues(int loopTime, float timeFactor)
    {
        this.interval = Math.max(1, loopTime);
        this.timeFactor = timeFactor;
        sync();
    }

    @Override
    public int loopTime()
    {
        return interval;
    }

    @Override
    public float timeFactor()
    {
        return timeFactor;
    }

    public void setStored(float value)
    {
        integral = 0;
        sync();
    }

    private void clear(DataVariant f)
    {
        integral = 0;
    }
}
