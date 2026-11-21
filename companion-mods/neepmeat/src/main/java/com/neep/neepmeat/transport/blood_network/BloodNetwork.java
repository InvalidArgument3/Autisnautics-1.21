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

package com.neep.neepmeat.transport.blood_network;

import com.neep.neepmeat.transport.api.pipe.VascularConduitEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface BloodNetwork extends BloodTransferChangeListener
{
    UUID getUUID();
    void rebuild(BlockPos pos, VascularConduitEntity.UpdateReason reason);
    void tick();
    void add(BlockPos pos, VascularConduitEntity newPart);
    void remove(BlockPos pos, VascularConduitEntity part);
    void merge(List<BloodNetwork> adjNetworks);

    void update(BlockPos pos, VascularConduitEntity part);

    void mergeInto(BloodNetwork other);

    static BloodNetwork find(Level world, BlockPos pos)
    {
        var conduit = VascularConduitEntity.find(world, pos);
        if (conduit != null)
        {
            return conduit.getNetwork();
        }
        return null;
    }

    boolean isRemoved();

    void insert(Collection<VascularConduitEntity> pipes);

    void unload(BlockPos pos, VascularConduitEntity part);
}
