/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.transport;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public final class PipeProperties {
    public static final EnumProperty<ConnectionType> NORTH = EnumProperty.create("north", ConnectionType.class);
    public static final EnumProperty<ConnectionType> SOUTH = EnumProperty.create("south", ConnectionType.class);
    public static final EnumProperty<ConnectionType> EAST = EnumProperty.create("east", ConnectionType.class);
    public static final EnumProperty<ConnectionType> WEST = EnumProperty.create("west", ConnectionType.class);
    public static final EnumProperty<ConnectionType> UP = EnumProperty.create("up", ConnectionType.class);
    public static final EnumProperty<ConnectionType> DOWN = EnumProperty.create("down", ConnectionType.class);

    private PipeProperties() {}

    public static EnumProperty<ConnectionType> of(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }
}
