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

public class ArithmeticBlockEntity extends SyncableBlockEntity implements ConfigProvider
{
    private float data1;
    private float data2;
    private float prevOutput;
    private int delay;

    private final CachingSender sender = new CachingSender(this::getLevel, getBlockPos());

    private final DirectReadPort output = new DirectReadPort(new SimpleEntry("Result"), () -> DataVariant.of(prevOutput), sender::send);
    private final WritePort input1 = this::receive1;
    private final WritePort input2 = this::receive2;
    private final ConfigEntry input1Entry = new SimpleEntry("Input 1");
    private final ConfigEntry input2Entry = new SimpleEntry("Input 2");

    private final NeepBusConfig config = NeepBusConfig.builder(this::sync)
            .input(input1Entry, input1)
            .input(input2Entry, input1)
            .output(output.entry(), output)
            .build();

    public ArithmeticBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    private void receive1(DataVariant data1)
    {
        calculate(data1, sender.read(input2Entry.getAddress()));
    }

    private void receive2(DataVariant data2)
    {
        calculate(sender.read(input1Entry.getAddress()), data2);
    }

    private void calculate(DataVariant input1, DataVariant input2)
    {
        this.data1 = input1.floatValue();
        this.data2 = input2.floatValue();
        level.scheduleTick(worldPosition, this.getBlockState().getBlock(), delay);
        setChanged();
    }

    public void onScheduledTick()
    {
        // Configurable operation
        prevOutput = data1 * data2;
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
        nbt.putFloat("data1", data1);
        nbt.putFloat("data2", data2);
        nbt.putFloat("prev_output", prevOutput);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.data1 = nbt.getFloat("data1");
        this.data2 = nbt.getFloat("data2");
        this.prevOutput = nbt.getFloat("prev_output");
    }

//    @Override
//    public int getDelay()
//    {
//        return delay;
//    }
//
//    @Override
//    public void setDelay(int delay)
//    {
//        this.delay = Math.max(1, delay);
//    }
}
