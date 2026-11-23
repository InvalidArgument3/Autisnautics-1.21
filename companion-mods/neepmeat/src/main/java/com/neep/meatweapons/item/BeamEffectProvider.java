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

package com.neep.meatweapons.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

@FunctionalInterface
public interface BeamEffectProvider
{
    BeamEffectProvider NONE = (world, pos, end, width, maxTime, showRadius, full) -> {};

    default void syncBeamEffect(ServerLevel world, Vec3 pos, Vec3 end, float width, int maxTime, double showRadius)
    {
        syncBeamEffect(world, pos, end, width, maxTime, showRadius, false);
    }

    void syncBeamEffect(ServerLevel world, Vec3 pos, Vec3 end, float width, int maxTime, double showRadius, boolean full);
}
