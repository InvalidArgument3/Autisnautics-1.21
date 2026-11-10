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

public class SampleAndHoldBlockEntity extends SyncableBlockEntity implements ConfigProvider
{
    private final CachingSender sender = new CachingSender(this::getLevel, worldPosition);

    private final ConfigEntry outputEntry = new SimpleEntry("SAMPLED VALUE");

    private final SimpleEntry inputEntry = new SimpleEntry("ADDRESS TO SAMPLE");

    private final WritePort input = this::input;
    private final SimpleReadPort output = new SimpleReadPort(outputEntry, sender::send);

    private final NeepBusConfig config = NeepBusConfig.builder(this::setChanged)
            .input(new SimpleEntry("SAMPLE"), this::triggerSample)
            .input(inputEntry, input)
            .output(outputEntry, output)
            .applyChanges(this)
            .build();

    public SampleAndHoldBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    private void triggerSample(DataVariant data)
    {
        output.set(sender.read(inputEntry.getAddress()));

        // tODO: tick gap
        sender.send(outputEntry.getAddress(), output.getValue());
    }

    private void input(DataVariant data)
    {

    }

    @Override
    public NeepBusConfig getConfig()
    {
        return config;
    }

    public void invalidateNetwork()
    {
        sender.invalidate();
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        output.writeNbt(nbt);
        config.writeNbt(nbt);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        output.readNbt(nbt);
        config.readNbt(nbt);
    }

    public void setValues(DataVariant value)
    {
        output.set(value);
        setChanged();
    }

    public DataVariant getValue()
    {
        return output.getValue();
    }
}
