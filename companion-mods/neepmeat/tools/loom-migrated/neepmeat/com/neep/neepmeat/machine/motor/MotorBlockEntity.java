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

package com.neep.neepmeat.machine.motor;

import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.neepmeat.api.PowerUtil;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class MotorBlockEntity extends LiquidFuelMachine implements MotorEntity
{
    public float rotorSpeed = 1f; // rad per tick
    public float currentSpeed = 0;
    public float angle;
    protected float outputPower = 0;
    protected float loadTorque;

    @Nullable protected BlockApiCache<MotorisedBlock, Void> cache = null;
    @Nullable private MotorisedBlock lastMotorised;

    public MotorBlockEntity(BlockEntityType<MotorBlockEntity> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void setBlockState(BlockState state)
    {
        super.setBlockState(state);
        if (lastMotorised != null)
            lastMotorised.setInputPower(0);
        cache = null;
    }

    public void tick()
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

        try (Transaction transaction = Transaction.openOuter())
        {
            long extracted = extractEnergy(maxPower(), transaction);

            float newPower;
            if (extracted > 0)
            {
                newPower = (float) PowerUtil.absoluteToPerUnit(extracted);
            }
            else
            {
                newPower = 0;
            }

            if (newPower != outputPower)
            {
                outputPower = newPower;
                onPowerChange();
            }

            transaction.commit();
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
    public float getRotorAngle()
    {
        return angle;
    }

    @Override
    public double getMechPUPower()
    {
        return outputPower;
    }

    @Override
    public MotorisedBlock getConnectedBlock()
    {
        return cache != null ? cache.find(null) : null;
    }

    @Override
    public float getSpeed()
    {
        double P = PowerUtil.perUnitToAbsWatt(getMechPUPower());
        return  (float) (P / (loadTorque != 0 ? loadTorque : PowerUtil.MOTOR_TORQUE_LOSS));
    }

    @Override
    protected long maxPower()
    {
        return NeepMeat.CONFIG.motors.basicMotorMaxPower.getRealValue();
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putFloat("loadTorque", loadTorque);
        nbt.putFloat("influx", outputPower);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.loadTorque = nbt.getFloat("loadTorque");
        this.outputPower = nbt.getFloat("influx");
    }
}
