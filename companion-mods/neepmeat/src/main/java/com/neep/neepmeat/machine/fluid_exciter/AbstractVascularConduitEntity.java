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

package com.neep.neepmeat.machine.fluid_exciter;

import com.neep.neepmeat.transport.api.pipe.VascularConduitEntity;
import com.neep.neepmeat.transport.blood_network.BloodNetwork;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public class AbstractVascularConduitEntity implements VascularConduitEntity
{
    private final BlockPos pos;
    @Nullable private BloodNetwork network;
    private boolean unloaded;

    public AbstractVascularConduitEntity(BlockPos pos)
    {
        this.pos = pos;
    }

    @Override
    public @Nullable BloodNetwork getNetwork()
    {
        return network;
    }

    @Override
    public void setNetwork(@Nullable BloodNetwork network)
    {
        this.network = network;
    }

    protected void onRemove()
    {
        if (network != null && !unloaded)
        {
            network.remove(pos, this);
        }
    }

    public void onUnload()
    {
        if (network != null)
        {
            network.unload(pos, this);
            unloaded = true;
        }
    }

    @Override
    public BlockPos getBlockPos()
    {
        return pos;
    }
}
