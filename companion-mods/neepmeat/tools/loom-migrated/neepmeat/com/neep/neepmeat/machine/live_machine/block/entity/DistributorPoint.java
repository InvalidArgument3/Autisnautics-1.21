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

package com.neep.neepmeat.machine.live_machine.block.entity;

import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.entity.skyworm.SkywormAnimationTimes;
import com.neep.neepmeat.transport.worm.DistributorParcel;
import com.neep.neepmeat.transport.worm.RouteLock;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface DistributorPoint extends LivingMachineComponent
{
    boolean isPowered();

    String getAddress();

    void setAddress(String addr, DistributorPointBlockEntity.Mode mode, int maxCooldown, boolean autoSend);

    DistributorPointBlockEntity.Mode getMode();

    void receive(DistributorParcel parcel);

    List<DistributorParcel> getQueuedParcels();

    Level getPointWorld();

    BlockPos getPointPos();

    void scheduleSend(RouteLock routeReference, SkywormAnimationTimes times);

    BlockPos getDumpPos();

    int getMaxCooldown();

    int getCooldown();

    boolean shouldAutoSend();
}
