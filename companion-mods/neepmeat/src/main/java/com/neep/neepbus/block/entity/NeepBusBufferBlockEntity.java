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
import com.neep.neepbus.NBTB;
import com.neep.neepbus.util.*;
import com.neep.neepbus.util.message.DataVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class NeepBusBufferBlockEntity extends SyncableBlockEntity implements ConfigProvider
{
    private final CachingSender sender = new CachingSender(this::getLevel, worldPosition);

    private final ConfigEntry outputEntry = new SimpleEntry("Value", new NBTB().r("Return the stored value").build());

    private final WritePort set = this::set;
    private final WritePort emit = this::emit;
    private final SimpleReadPort output = new SimpleReadPort(outputEntry, sender::send);

    private final NeepBusConfig config = NeepBusConfig.builder(this::setChanged)
            .input(new SimpleEntry("Set", new NBTB().w("Set stored value").build()), set)
            .input(new SimpleEntry("Send", new NBTB().w("Make the output port write the stored value to all matching addresses").build()), emit)
            .output(outputEntry, output)
            .applyChanges(this)
            .build();

    public NeepBusBufferBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    private void set(DataVariant data)
    {
        output.set(data);
    }

    private void emit(DataVariant data)
    {
        output.send();
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
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        output.writeNbt(nbt);
        config.writeNbt(nbt);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
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
