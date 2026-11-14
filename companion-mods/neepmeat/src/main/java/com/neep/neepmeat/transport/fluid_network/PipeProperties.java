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

package com.neep.neepmeat.transport.fluid_network;

import net.minecraft.world.level.block.state.properties.EnumProperty;

public class PipeProperties
{
    public static final EnumProperty<PipeConnectionType> NORTH_CONNECTION = EnumProperty.create("north", PipeConnectionType.class);
    public static final EnumProperty<PipeConnectionType> SOUTH_CONNECTION = EnumProperty.create("south", PipeConnectionType.class);
    public static final EnumProperty<PipeConnectionType> EAST_CONNECTION = EnumProperty.create("east", PipeConnectionType.class);
    public static final EnumProperty<PipeConnectionType> WEST_CONNECTION = EnumProperty.create("west", PipeConnectionType.class);
    public static final EnumProperty<PipeConnectionType> UP_CONNECTION = EnumProperty.create("up", PipeConnectionType.class);
    public static final EnumProperty<PipeConnectionType> DOWN_CONNECTION = EnumProperty.create("down", PipeConnectionType.class);
}
