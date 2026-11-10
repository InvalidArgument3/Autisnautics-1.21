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

package com.neep.neepmeat.block.entity;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.machine.MotorisedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class MotorisedMachineBlockEntity extends SyncableBlockEntity implements MotorisedBlock, MotorisedBlock.DiagnosticsProvider
{
    protected float power;
    protected float lastPower;
    protected float progressIncrement;
    public final float minPower;
    protected final float maxIncrement;

    public MotorisedMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, float minPower, float maxIncrement)
    {
        super(type, pos, state);
        this.minPower = minPower;
        this.maxIncrement = maxIncrement;
    }

    @Override
    public MotorisedBlock.Diagnostics getDiagnostics()
    {
        return MotorisedBlock.Diagnostics.insufficientPower(power < minPower, power, minPower);
    }

    public void serverTick()
    {
        if (lastPower != power)
        {
            lastPower = power;
            sync();
        }
    }

    @Override
    public void setInputPower(float power)
    {
        if (this.power != power)
        {
            this.power = power;
            if (power < minPower)
                progressIncrement = 0;
            else
                progressIncrement = Math.min(power, maxIncrement);

            sync();
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        nbt.putFloat("power", power);
        nbt.putFloat("increment", progressIncrement);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.power = nbt.getFloat("power");
        this.progressIncrement = nbt.getFloat("increment");
    }

    public float progressIncrement()
    {
        return progressIncrement;
    }

    public float maxIncrement()
    {
        return maxIncrement;
    }
}