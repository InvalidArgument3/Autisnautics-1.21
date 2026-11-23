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

package com.neep.neepmeat.api;

import com.neep.neepmeat.NeepMeat;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public interface Burner
{
    BlockApiLookup<Burner, Direction> LOOKUP =
            BlockApiLookup.get(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "burner_lookup"), Burner.class, Direction.class);

    default void tickPowerConsumption() {};

    double getOutputPower();
}
