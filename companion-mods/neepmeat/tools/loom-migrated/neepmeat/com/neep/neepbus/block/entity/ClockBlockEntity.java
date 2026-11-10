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

public class ClockBlockEntity extends SyncableBlockEntity implements ConfigProvider
{
    private final CachingSender sender = new CachingSender(this::getLevel, worldPosition);

    private final DirectReadPort output = new DirectReadPort(new SimpleEntry("CLOCK SIGNAL"), () -> DataVariant.of(1), sender::send);

    private final NeepBusConfig config = NeepBusConfig.builder(this::sync)
            .output(output.entry(), output)
            .applyChanges(this)
            .build();

    private int interval = 10;

    public ClockBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
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
        nbt.putInt("interval", interval);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        config.readNbt(nbt);
        this.interval = nbt.getInt("interval");
    }

    public void invalidate()
    {
        sender.invalidate();
    }

    public void serverTick()
    {
        if (interval > 0 && level.getGameTime() % interval == 0)
        {
            output.send();
        }
    }

    public void setValues(int loopTime)
    {
        this.interval = loopTime;
        sync();
    }

//    @Override
//    public int loopTime()
//    {
//        return interval;
//    }
}
