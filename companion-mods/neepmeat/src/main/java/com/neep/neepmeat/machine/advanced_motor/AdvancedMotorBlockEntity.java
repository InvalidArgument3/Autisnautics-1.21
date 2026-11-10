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

package com.neep.neepmeat.machine.advanced_motor;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.machine.motor.MotorEntity;
import com.neep.neepmeat.transport.api.pipe.AbstractBloodAcceptor;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AdvancedMotorBlockEntity extends SyncableBlockEntity implements MotorEntity
{
    public float rotorSpeed = 1f; // rad per tick
    public float currentSpeed = 0;
    public float angle;
    protected float influx;

    protected float loadTorque;
    @Nullable protected MotorisedBlock lastMotorised;

    protected final float maxInflux = (float) NeepMeat.CONFIG.motors.advancedMotorMaxPower.getRealValue() / PowerUtil.BASE_POWER;

    @Nullable protected BlockApiCache<MotorisedBlock, Void> cache = null;

    protected final BloodAcceptor bloodAcceptor = new AbstractBloodAcceptor()
    {
        @Override
        public float updateInflux(float influx)
        {
            AdvancedMotorBlockEntity.this.influx = Math.min(influx, maxInflux);
            onPowerChange();
            return influx;
        }

        @Override
        public Mode getMode()
        {
            return Mode.SINK;
        }
    };

    public AdvancedMotorBlockEntity(BlockEntityType<AdvancedMotorBlockEntity> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public void serverTick()
    {
        if (cache == null)
        {
            Direction facing = getBlockState().getValue(BaseFacingBlock.FACING);
            cache = BlockApiCache.create(MotorisedBlock.LOOKUP, (ServerLevel) level, worldPosition.relative(facing));
        }

        MotorisedBlock motorised = cache.find(null);
        if (motorised != null)
        {
            if (motorised.getLoadTorque() != loadTorque)
            {
                loadTorque = motorised.getLoadTorque();
                sync();
            }

            if (motorised != lastMotorised)
            {
                lastMotorised = motorised;
                onPowerChange();
            }

            motorised.motorTick(this);
        }
        else
        {
            lastMotorised = null;
        }
    }

    protected void onPowerChange()
    {
        MotorisedBlock motorised = getConnectedBlock();
        if (motorised != null)
        {
            // TODO: Decide on float or double
            motorised.setInputPower((float) this.getMechPUPower());
        }
        sync();
    }

    @Override
    public void setBlockState(BlockState state)
    {
        super.setBlockState(state);
        if (lastMotorised != null)
            lastMotorised.setInputPower(0);
        cache = null;
    }

    @Override
    public float getRotorAngle()
    {
        return angle;
    }

    @Override
    public MotorisedBlock getConnectedBlock()
    {
        return cache != null ? cache.find(null) : null;
    }

    @Override
    public double getMechPUPower()
    {
        return influx;
    }

    @Override
    public float getSpeed()
    {
        double power = PowerUtil.perUnitToAbsWatt(getMechPUPower());
        return (float) (power / (loadTorque != 0 ? loadTorque : PowerUtil.MOTOR_TORQUE_LOSS));
    }

    @Override
    public void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        nbt.putFloat("loadTorque", loadTorque);
        nbt.putFloat("influx", influx);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.loadTorque = nbt.getFloat("loadTorque");
        this.influx = nbt.getFloat("influx");
    }

    public BloodAcceptor getBloodAcceptor(Direction face)
    {
        return bloodAcceptor;
    }
}
