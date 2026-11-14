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

package com.neep.neepbus.util;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.nbt.CompoundTag;

public class ConfigDelegate implements NeepBusConfig
{
    private final NeepBusConfig config;
    private final int ports;

    @Nullable private PortMap portMap;

    // Not sure how useful multiple ports per side will be.
    public ConfigDelegate(NeepBusConfig config, int ports)
    {
        this.config = config;
        this.ports = ports;

    }

    public void invalidate()
    {
        portMap = null;
    }

    @Override
    public PortMap getPorts()
    {
        if (portMap == null)
        {
            PortMap backingMap = config.getPorts();
            portMap = backingMap.delegate(ports);
        }
        return portMap;
    }

    @Override
    public List<? extends ConfigEntry> getInputs()
    {
        return List.of();
    }

    @Override
    public List<? extends ConfigEntry> getOutputs()
    {
        return List.of();
    }

    @Override
    public void applyChanges()
    {

    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {

    }
}
