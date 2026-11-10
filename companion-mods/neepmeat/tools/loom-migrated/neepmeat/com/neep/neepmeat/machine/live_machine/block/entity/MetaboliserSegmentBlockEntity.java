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

package com.neep.neepmeat.machine.live_machine.block.entity;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.component.MetaboliserSegmentComponent;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MetaboliserSegmentBlockEntity extends SyncableBlockEntity implements MetaboliserSegmentComponent
{
    private final BloodAcceptor bloodAcceptor = new Acceptor();

    private long energy;
    private long lastEnergy; // For display

    public MetaboliserSegmentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public long getEnergy()
    {
        return lastEnergy;
    }

    @Override
    public boolean componentRemoved()
    {
        return isRemoved();
    }

    @Override
    public void setController(@Nullable BlockPos pos)
    {
        // energy will be depleted when consumed, so there is no need to set it too
        // In fact, that would cause a 1 tick loss when setController is called
        if (pos == null)
            lastEnergy = 0;
    }

    @Override
    public void shutdown()
    {
        MetaboliserSegmentComponent.super.shutdown();
        lastEnergy = 0;
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType()
    {
        return LivingMachineComponents.METABOLISER_SEGMENT;
    }

    public BloodAcceptor getBloodAcceptor()
    {
        return bloodAcceptor;
    }

    @Override
    public void setOutputEnergy(long amount)
    {
        energy = amount;
        lastEnergy = amount;
    }

    class Acceptor implements BloodAcceptor
    {
        @Override
        public Mode getMode()
        {
            return Mode.SOURCE;
        }

        @Override
        public long getOutput()
        {
            var energy1 = energy;
            energy = 0;
            return energy1;
        }
    }

}
