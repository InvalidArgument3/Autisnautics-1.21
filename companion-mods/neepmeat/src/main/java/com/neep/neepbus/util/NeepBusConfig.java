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

import com.neep.meatlib.util.NbtSerialisable;
import java.util.List;
import net.minecraft.nbt.CompoundTag;

public interface NeepBusConfig extends NbtSerialisable
{
    static NeepBusConfigImpl.Builder builder(Runnable markDirty)
    {
        return new NeepBusConfigImpl.Builder(markDirty);
    }

    PortMap getPorts();

    List<? extends ConfigEntry> getInputs();
    List<? extends ConfigEntry> getOutputs();

    /**
     * To be called after input port changes have been made.
     * Particularly relevant when an input address has changed to match an output address elsewhere.
     * Override to emit a flood update through the cable network.
     */
    void applyChanges();

    NeepBusConfig EMPTY = new NeepBusConfig()
    {
        @Override
        public PortMap getPorts()
        {
            return PortMap.EMPTY;
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
    };
}
