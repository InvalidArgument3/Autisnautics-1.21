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

public class SimplePipeProperties
{
    public static final EnumProperty<ConnectionType> NORTH_CONNECTION = EnumProperty.create("north", ConnectionType.class);
    public static final EnumProperty<ConnectionType> SOUTH_CONNECTION = EnumProperty.create("south", ConnectionType.class);
    public static final EnumProperty<ConnectionType> EAST_CONNECTION = EnumProperty.create("east", ConnectionType.class);
    public static final EnumProperty<ConnectionType> WEST_CONNECTION = EnumProperty.create("west", ConnectionType.class);
    public static final EnumProperty<ConnectionType> UP_CONNECTION = EnumProperty.create("up", ConnectionType.class);
    public static final EnumProperty<ConnectionType> DOWN_CONNECTION = EnumProperty.create("down", ConnectionType.class);
}
