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

package com.neep.neepmeat.plc.block.entity;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepbus.block.entity.ConfigProvider;
import com.neep.neepbus.util.*;
import com.neep.neepbus.util.message.DataVariant;
import com.neep.neepmeat.api.plc.interrupt.InterruptEmitter;
import com.neep.neepmeat.api.plc.interrupt.InterruptReceiver;
import com.neep.neepmeat.api.plc.memory.MemoryProvider;
import com.neep.neepmeat.plc.memory.MemoryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PLCNeepBusInterfaceBlockEntity extends SyncableBlockEntity implements InterruptEmitter, ConfigProvider, MemoryProvider
{
    private final CachingSender sender = new CachingSender(this::getLevel, worldPosition);

    private final ConfigEntry writeEntry = new SimpleEntry("Input", "On data write:\nEmit an interrupt and stores the received data.");
    private final WritePort writePort = this::receive;

    private final ConfigEntry readEntry = new SimpleEntry("Output", "On data read:\nReturn null\nOn memory value sent via PLC:\nWrite given data immediately.");
    private final SimpleReadPort readPort = new SimpleReadPort(readEntry, sender::send);

    private final NeepBusConfig config = NeepBusConfig.builder(this::sync)
            .input(writeEntry, writePort)
            .output(readEntry, readPort)
            .applyChanges(this)
            .build();

    private final Set<InterruptReceiver> listeners = new HashSet<>();
    private DataVariant data = DataVariant.NULL;

    public PLCNeepBusInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    private void receive(DataVariant data)
    {
        this.data = data;
        listeners.forEach(l -> l.interrupt(worldPosition, this));
        setChanged();
    }

    public void invalidate()
    {
        sender.invalidate();
    }

    @Override
    public void addListener(InterruptReceiver receiver)
    {
        listeners.add(receiver);
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
        readPort.writeNbt(nbt);
        nbt.putString("data", data.stringValue());
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        config.readNbt(nbt);
        readPort.readNbt(nbt);
        this.data = DataVariant.of(nbt.getString("data"));
    }

    @Override
    public @Nullable MemoryEntry[] poll()
    {
        return new MemoryEntry[]{MemoryEntry.of(data)};
    }

    @Override
    public void push(MemoryEntry entry)
    {
        readPort.send(entry.toDataVariant());
    }
}
