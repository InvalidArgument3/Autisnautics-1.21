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

import com.neep.neepmeat.api.plc.interrupt.InterruptEmitter;
import com.neep.neepmeat.api.plc.interrupt.InterruptReceiver;
import com.neep.neepmeat.plc.block.RedstoneInterface;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PLCRedstoneInterfaceBlockEntity extends BlockEntity implements RedstoneInterface, InterruptEmitter
{
    private int emitted;
    private int received;
    private final Set<InterruptReceiver> listeners = new HashSet<>();

    private boolean emitInterrupt;
    private boolean updateNeighbors;

    public PLCRedstoneInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public int getReceivedStrength()
    {
        return received;
    }

    public int getEmittedStrength()
    {
        return emitted;
    }

    @Override
    public void setEmittedStrength(int strength)
    {
        if (emitted != strength)
        {
            emitted = strength;
            setChanged();

            updateNeighbors = true;
            emitUpdate();
        }
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.emitted = nbt.getInt("emitted");
        this.received = nbt.getInt("received");
    }

    @Override
    protected void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putInt("emitted", emitted);
        nbt.putInt("received", received);
    }

    private void emitUpdate()
    {
        if (level != null)
        {
            // Prevent duplicates leading to multiple block updates or PLC interrupts per tick
            if (!level.getBlockTicks().hasScheduledTick(worldPosition, getBlockState().getBlock()))
                level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
        }
    }

    public void updateReceived(int received)
    {
        if (this.received != received)
        {
            this.received = received;
            setChanged();

            emitInterrupt = true;
            emitUpdate();
        }
    }

    public void onScheduledTick()
    {
        if (emitInterrupt)
        {
            for (InterruptReceiver listener : listeners)
            {
                listener.interrupt(worldPosition, this);
            }
        }

        if (updateNeighbors)
        {
            level.blockUpdated(worldPosition, getBlockState().getBlock());
        }
    }

    @Override
    public void addListener(InterruptReceiver receiver)
    {
        listeners.add(receiver);
    }
}
