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
import com.neep.neepbus.part.Indicator;
import com.neep.neepbus.util.NeepBusConfig;
import com.neep.neepbus.util.SimpleEntry;
import com.neep.neepbus.util.WritePort;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class VerticalGaugeBlockEntity extends SyncableBlockEntity implements Indicator, GaugeBlockEntity, ConfigProvider
{
    private float value;
    private float minValue = 0;
    private float maxValue = 500;

    private final WritePort inputPort = data ->
    {
        if (value != data.floatValue())
        {
            value = data.floatValue();
            sync();
        }
    };

    private final NeepBusConfig config = NeepBusConfig.builder(this::sync)
            .input(new SimpleEntry("Input"), inputPort)
            .applyChanges(this)
            .build();

    private String name = "";
    private String unit = "";
    private boolean usePrefix = true;

    public VerticalGaugeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        toClientTag(nbt);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        fromClientTag(nbt);
    }

    @Override
    public void fromClientTag(CompoundTag nbt)
    {
        this.value = nbt.getFloat("value");
        this.minValue = nbt.getFloat("min_value");
        this.maxValue = nbt.getFloat("max_value");
        this.config.readNbt(nbt.getCompound("config"));
        this.name = nbt.getString("name");
        this.unit = nbt.getString("unit");
        this.usePrefix = nbt.getBoolean("use_prefix");
    }

    @Override
    public void toClientTag(CompoundTag nbt)
    {
        nbt.putFloat("value", value);
        nbt.putFloat("min_value", minValue);
        nbt.putFloat("max_value", maxValue);
        nbt.put("config", config.writeNbt(new CompoundTag()));
        nbt.putString("name", name);
        nbt.putString("unit", unit);
        nbt.putBoolean("use_prefix", usePrefix);
    }

    public NeepBusConfig getConfig()
    {
        return config;
    }

    @Override
    public @Nullable String getName()
    {
        return name;
    }

    @Override
    public float getValue() { return value; }

    @Override
    public boolean getUsePrefix()
    {
        return usePrefix;
    }

    @Override
    public String getUnit()
    {
        return unit;
    }

    @Override
    public float getMinValue() { return minValue; }

    @Override
    public float getMaxValue() { return maxValue; }

    @Override
    public void setValues(float value, float minValue, float maxValue, String name, boolean usePrefix, String unit)
    {
        this.value = value;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.name = name;
        this.usePrefix = usePrefix;
        this.unit = unit;
        sync();
    }

}
