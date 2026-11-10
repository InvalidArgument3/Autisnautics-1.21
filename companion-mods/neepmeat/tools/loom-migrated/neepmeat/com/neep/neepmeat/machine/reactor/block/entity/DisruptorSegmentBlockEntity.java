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
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.component.PoweredComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DisruptorSegmentBlockEntity extends SyncableBlockEntity implements LivingMachineComponent, PoweredComponent
{
    private float progressIncrement;
    private int disruption;

    public DisruptorSegmentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public boolean componentRemoved()
    {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType()
    {
        return LivingMachineComponents.DISRUPTOR_SEGMENT;
    }

    public float progressIncrement()
    {
        return progressIncrement;
    }

    @Override
    public void setProgressIncrement(float progressIncrement)
    {
        if (this.progressIncrement != progressIncrement)
        {
            this.progressIncrement = progressIncrement;
            sync();
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putInt("disruption", disruption);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.disruption = nbt.getInt("disruption");
    }

    @Override
    public void fromClientTag(CompoundTag nbt)
    {
        load(nbt);
        this.progressIncrement = nbt.getFloat("progress_increment");
    }

    @Override
    public void toClientTag(CompoundTag nbt)
    {
        saveAdditional(nbt);
        nbt.putFloat("progress_increment", progressIncrement);
    }

    public void setDisruption(int disruption)
    {
        if (this.disruption != disruption)
        {
            this.disruption = disruption;
            sync();
        }
    }

    public int getDisruption()
    {
        return disruption;
    }
}
