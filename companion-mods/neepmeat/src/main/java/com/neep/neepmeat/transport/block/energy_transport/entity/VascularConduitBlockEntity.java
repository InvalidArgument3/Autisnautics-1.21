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

import com.neep.neepmeat.transport.api.BlockEntityUnloadListener;
import com.neep.neepmeat.transport.api.pipe.VascularConduitEntity;
import com.neep.neepmeat.transport.blood_network.BloodNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

public class VascularConduitBlockEntity extends BlockEntity implements VascularConduitEntity, BlockEntityUnloadListener
{
    protected BloodNetwork network;

    public VascularConduitBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void setLevel(Level world)
    {
        super.setLevel(world);
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
        setChanged();
    }

    private boolean unloaded;

    @Override
    public void setRemoved()
    {
        if (network != null && !unloaded)
        {
            // network.remove has some extra stuff for updating nearby acceptors.
            // If this happens when the world is unloading, it will cause the chunks to load again which
            // probably has unintended consequences.
            network.remove(worldPosition, this);
        }
        super.setRemoved();
    }

    @Override
    public void onUnload(LevelChunk chunk)
    {
        if (network != null)
        {
            network.unload(worldPosition, this);
            unloaded = true;
        }
    }
}
