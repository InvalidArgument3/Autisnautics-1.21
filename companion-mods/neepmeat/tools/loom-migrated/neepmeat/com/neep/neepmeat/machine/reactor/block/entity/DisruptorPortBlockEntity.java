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

package com.neep.neepmeat.machine.reactor.block.entity;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepbus.block.entity.ConfigProvider;
import com.neep.neepbus.util.NeepBusConfig;
import com.neep.neepbus.util.SimpleEntry;
import com.neep.neepbus.util.message.DataVariant;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DisruptorPortBlockEntity extends SyncableBlockEntity implements ConfigProvider, LivingMachineComponent
{
    private final NeepBusConfig config = NeepBusConfig.builder(this::sync)
            .input(new SimpleEntry("Disruptor Power"), this::onWrite)
            .applyChanges(this)
            .build();

    private int disruption;

    @Nullable private ReactionCoreBlockEntity core;

    public DisruptorPortBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    private void onWrite(DataVariant data)
    {
        this.disruption = data.intValue();
    }

    @Override
    public NeepBusConfig getConfig()
    {
        return config;
    }

    @Override
    public boolean componentRemoved()
    {
        return isRemoved();
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.put("config", config.writeNbt(new CompoundTag()));
        nbt.putInt("disruption", disruption);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        config.readNbt(nbt.getCompound("config"));
        this.disruption = nbt.getInt("disruption");
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType()
    {
        return LivingMachineComponents.DISRUPTOR_PORT;
    }

    public int getDisruption()
    {
        return disruption;
    }

    public void setCore(@Nullable ReactionCoreBlockEntity core)
    {
        this.core = core;
    }

    @Nullable
    public ReactionCoreBlockEntity getCore()
    {
        return core;
    }
}
