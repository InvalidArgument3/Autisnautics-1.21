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

package com.neep.neepmeat.util;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public final class NMMaths {
    private NMMaths() {}

    public static int wrapIndex(int index, int size) {
        if (size <= 0) {
            return 0;
        }
        int mod = index % size;
        return mod < 0 ? mod + size : mod;
    }

    public static boolean isWithin(float value, float target, float difference) {
        return Math.abs(target - value) < difference;
    }

    public static NMVec2f removeAxis(Direction.Axis axis, Vec3 vec) {
        return switch (axis) {
            case X -> new NMVec2f((float) vec.y, (float) vec.z);
            case Y -> new NMVec2f((float) vec.x, (float) vec.z);
            case Z -> new NMVec2f((float) vec.x, (float) vec.y);
        };
    }

    /** The wrench hit-box rotation upstream uses when the player clicks a horizontal face. */
    public static Direction swapDirections(Direction direction) {
        return switch (direction) {
            case UP -> Direction.EAST;
            case DOWN -> Direction.WEST;
            case EAST -> Direction.DOWN;
            case WEST -> Direction.UP;
            default -> throw new IllegalStateException("Unable to get X-rotated facing of " + direction);
        };
    }
}
