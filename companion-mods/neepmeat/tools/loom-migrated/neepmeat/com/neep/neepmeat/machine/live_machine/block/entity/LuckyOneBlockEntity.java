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
import com.neep.neepmeat.transport.api.pipe.AbstractBloodAcceptor;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class LuckyOneBlockEntity extends SyncableBlockEntity implements LivingMachineComponent
{
    private final AbstractBloodAcceptor acceptor = new AbstractBloodAcceptor()
    {
        @Override
        public Mode getMode()
        {
            return Mode.SINK;
        }

        @Override
        public float updateInflux(float influx)
        {
            if (influx >= 0.05)
            {
                LuckyOneBlockEntity.this.influx = 0.05f;
                sync();
                return 0.05f;
            }
            else
            {
                LuckyOneBlockEntity.this.influx = 0;
                sync();
                return 0;
            }
        }
    };

    public int animationOffset = -1;
    private float influx;

    public LuckyOneBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void setLevel(Level world)
    {
        super.setLevel(world);
        if (animationOffset == -1)
        {
            animationOffset = world.getRandom().nextInt(200);
            setChanged();
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putFloat("influx", influx);
        nbt.putInt("start_time", animationOffset);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.influx = nbt.getFloat("influx");
        this.animationOffset = nbt.getInt("start_time");
    }

    public float getInflux()
    {
        return influx;
    }

    public BloodAcceptor getAcceptor(Direction unused)
    {
        return acceptor;
    }

    @Override
    public boolean componentRemoved()
    {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType()
    {
        return LivingMachineComponents.LUCKY_ONE;
    }

    public boolean isActive()
    {
        return influx >= 0.05;
    }
}
