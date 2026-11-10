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
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PIDControllerBlockEntity extends SyncableBlockEntity implements ConfigProvider, PIDController
{
    private final CachingSender sender = new CachingSender(this::getLevel, getBlockPos());

    private final DirectReadPort outputPort = new DirectReadPort(new SimpleEntry("PID Output"), this::getOutput, sender::send);

    private final SimpleEntry setpointPort = new SimpleEntry("Setpoint");
    private final SimpleEntry measurement = new SimpleEntry("Measurement");

    private final NeepBusConfig config = NeepBusConfig.builder(this::sync)
            .output(outputPort.entry(), outputPort)
            .input(setpointPort, this::setSetPoint)
            .input(measurement, this::setMeasured)
            .build();

    private int dt = 2;

    protected float lastError;
    protected float integral;

    protected float kp = 1;
    protected float ki = 0;
    protected float kd = 0;
    protected float windupLimit = 1;

    protected float output;

    public PIDControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    private DataVariant getOutput()
    {
        return DataVariant.of(output);
    }

    private void setSetPoint(DataVariant i)
    {

    }

    private void setMeasured(DataVariant i)
    {

    }

    public void serverTick()
    {
        if (level.getGameTime() % dt == 0)
        {
            float measured = sender.read(measurement.getAddress()).floatValue();
            float setpoint = sender.read(setpointPort.getAddress()).floatValue();

            float error = setpoint - measured;

            integral = Mth.clamp(integral + error * dt, -windupLimit, windupLimit);
            float derivative = (error - lastError) / dt;

            output = kp * error + ki * integral + kd * derivative;
            outputPort.send();

            lastError = error;
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.put("config", config.writeNbt(new CompoundTag()));
        nbt.putInt("dt", dt);
        nbt.putFloat("kp", kp);
        nbt.putFloat("ki", ki);
        nbt.putFloat("kd", kd);
        nbt.putFloat("windup_limit", windupLimit);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        config.readNbt(nbt.getCompound("config"));
        this.dt = nbt.getInt("dt");
        this.kp = nbt.getFloat("kp");
        this.ki = nbt.getFloat("ki");
        this.kd = nbt.getFloat("kd");
        this.windupLimit = nbt.getFloat("windup_limit");
    }

    @Override
    public NeepBusConfig getConfig()
    {
        return config;
    }

    @Override
    public int getDt()
    {
        return dt;
    }

    @Override
    public void setDt(int dt)
    {
        this.dt = Math.max(1, dt);
    }

    @Override
    public void setKP(float kp)
    {
        this.kp = kp;
    }

    @Override
    public float getKP()
    {
        return kp;
    }

    @Override
    public void setKI(float ki)
    {
        this.ki = ki;
    }

    @Override
    public float getKI()
    {
        return ki;
    }

    @Override
    public void setKD(float kd)
    {
        this.kd = kd;
    }

    @Override
    public float getKD()
    {
        return kd;
    }

    @Override
    public void setWindupLimit(float windupLimit)
    {
        this.windupLimit = windupLimit;
    }

    @Override
    public float getWindupLimit()
    {
        return windupLimit;
    }

    @Override
    public void markBlockDirty()
    {
        sync();
    }

    public void invalidate()
    {
        sender.invalidate();
    }

}
