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
 *
 * Cardinal Components stored this on the world. Here it is a per-level map.
 * The skyworm flight is not spawned.
 */

package com.neep.neepmeat.transport.worm;

import com.neep.neepmeat.entity.skyworm.SkywormAnimationTimes;
import com.neep.neepmeat.machine.live_machine.block.entity.DistributorPoint;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DistributorRouting {
    private static final Map<Level, DistributorRouting> ALL = new HashMap<>();

    private final Level world;
    private final Map<BlockPos, PointHandle> handles = new HashMap<>();
    private final Set<BlockPos> blocked = new HashSet<>();
    private final List<DistributorParcel> sent = new ArrayList<>();

    private DistributorRouting(Level world) {
        this.world = world;
    }

    public static DistributorRouting get(Level world) {
        return ALL.computeIfAbsent(world, DistributorRouting::new);
    }

    public void registerPoint(DistributorPoint point) {
        handles.put(point.getPointPos(), new PointHandle(point.getPointPos(), point.getAddress(), point.getMode(), point.isPowered()));
    }

    public void send(DistributorParcel parcel, DistributorDestination destination, SkywormAnimationTimes times) {
        sent.add(parcel);
        blocked.add(destination.pos());
    }

    public RouteLock acquireRoute(BlockPos pos) {
        blocked.add(pos);
        return new RouteLock(world, pos);
    }

    public void releaseRoute(BlockPos pos) {
        blocked.remove(pos);
    }

    public boolean isBlocked(BlockPos pos) {
        return blocked.contains(pos);
    }

    public int sentCount() {
        return sent.size();
    }
}
