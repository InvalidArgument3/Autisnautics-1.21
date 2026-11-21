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

package com.neep.neepmeat.transport.worm;

import com.neep.neepmeat.entity.skyworm.SkywormAnimationTimes;
import com.neep.neepmeat.machine.live_machine.block.entity.DistributorPoint;
import dev.onyxstudios.cca.api.v3.component.Component;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface DistributorRoutingComponent extends Component
{
    void collectMatching(List<DistributorDestination> result, String address);

    Level getWorld();

    void registerPoint(DistributorPoint point);

    void unregisterPoint(BlockPos pos);

    void send(DistributorParcel parcel, DistributorDestination destination, SkywormAnimationTimes times);

    RouteLock acquireRoute(BlockPos pos);

    void releaseRoute(BlockPos pos);
}
