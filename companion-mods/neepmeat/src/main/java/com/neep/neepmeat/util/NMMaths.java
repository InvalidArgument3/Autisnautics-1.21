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
import net.minecraft.util.Mth;
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

    /** Upstream look vector. Pitch and yaw are radians. */
    public static Vec3 getRotationVector(double pitchRad, double yawRad) {
        float pitch = (float) pitchRad;
        float yaw = (float) -yawRad;
        float cosYaw = Mth.cos(yaw);
        float sinYaw = Mth.sin(yaw);
        float cosPitch = Mth.cos(pitch);
        float sinPitch = Mth.sin(pitch);
        return new Vec3(sinYaw * cosPitch, -sinPitch, cosYaw * cosPitch);
    }

    public static float wrapRadians(float radians) {
        float tau = (float) (Math.PI * 2);
        return radians - tau * (float) Math.floor((radians + Math.PI) / tau);
    }

    public static float moveTowards(float current, float target, float speed) {
        float difference = target - current;
        if (Math.abs(difference) > 0) {
            return current + Math.signum(difference) * Math.min(speed, Math.abs(difference));
        }
        return target;
    }

    public static float deltaAngle(float radians1, float radians2) {
        float tau = (float) (Math.PI * 2);
        float diff = (radians2 - radians1 + (float) Math.PI) % tau - (float) Math.PI;
        return diff < -(float) Math.PI ? diff + tau : diff;
    }

    public static float moveTowardsAngle(float current, float target, float speed) {
        float difference = deltaAngle(current, target);
        if (Math.abs(difference) > 1.0e-5f) {
            return current + Math.signum(difference) * Math.min(speed, Math.abs(difference));
        }
        return target;
    }

    public static float log10(float value) {
        return (float) Math.log10(value);
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
