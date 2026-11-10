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

package com.neep.neepmeat.transport.block.energy_transport.entity;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.transport.api.pipe.AbstractBloodAcceptor;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PowerEmitterBlockEntity extends SyncableBlockEntity
{
    private int powerOutput = 100;

    private final AbstractBloodAcceptor bloodAcceptor = new AbstractBloodAcceptor()
    {
        @Override
        public Mode getMode()
        {
            return Mode.SOURCE;
        }

        @Override
        public long getOutput()
        {
            return powerOutput;
        }
    };

    public PowerEmitterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        nbt.putInt("power", powerOutput);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.powerOutput = nbt.getInt("power");
    }

    public void setPower(int powerOutput)
    {
        this.powerOutput = Math.max(0, powerOutput);
        setChanged();

        var network = bloodAcceptor.getNetwork();
        if (network != null)
            network.updateTransfer(bloodAcceptor);
    }

    public int getOutputPower()
    {
        return powerOutput;
    }

    public BloodAcceptor getBloodAcceptor(Direction ignored)
    {
        return bloodAcceptor;
    }
}
