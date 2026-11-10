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

import com.neep.meatweapons.mixin.BoxMixin;
import com.neep.neepmeat.transport.fluid_network.node.FluidNode;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.math.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.framework.qual.NoDefaultQualifierForUse;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.core.Direction.*;

public class NMMaths
{
    public static boolean isWithin(float value, float target, float difference)
    {
        return Math.abs(target - value) < difference;
    }

    public static NMVec2f removeAxis(Direction.Axis axis, Vec3 vec)
    {
        NMVec2f out;
        switch (axis)
        {
            case X -> out = new NMVec2f((float) vec.y(), (float) vec.z());
            case Y -> out = new NMVec2f((float) vec.x(), (float) vec.z());
            case Z -> out = new NMVec2f((float) vec.x(), (float) vec.y());
            default -> throw new IllegalStateException("Unexpected value: " + axis);
        }
        return out;
    }

    public static float minMag(float a, float b)
    {
        return Math.abs(a) < Math.abs(b) ? a : b;
    }

    // An ugly bodge for a very specific situation.

    public static Direction swapDirections(Direction direction)
    {
        switch (direction)
        {
            case UP ->
            {
                return EAST;
            }
            case DOWN ->
            {
                return WEST;
            }
            case EAST ->
            {
                return DOWN;
            }
            case WEST ->
            {
                return UP;
            }
        }
        throw new IllegalStateException("Unable to get X-rotated facing of " + direction);
    }
    public static double manhattanDistance(Vec3 v1, Vec3 v2)
    {
        double d = Math.abs(v2.x - v1.x);
        double e = Math.abs(v2.y - v1.y);
        double f = Math.abs(v2.z - v1.z);
        return d + e + f;
    }

    public static Vec2 flattenY(Vec3 vec)
    {
        return new Vec2((float) vec.x(), (float) vec.z());
    }

    public static float getAngle(Vec3 v1, Vec3 v2)
    {
        return (float) Math.acos(v1.dot(v2) / (v1.length() * v2.length()));
    }

    public static float getAngle(Vec2 v1)
    {
        return (float) ((float) Math.atan2(v1.x, v1.y) + Math.PI);
    }

    public static float angleLerp(float delta, float x1, float x2)
    {
        // StackOverflow magic
        float CS = (1 - delta) * Mth.cos(x1) + delta * Mth.cos(x2);
        float SN = (1 - delta) * Mth.sin(x1) + delta * Mth.sin(x2);
        var C = Mth.atan2(SN, CS);
        return (float) C;
    }

    public static float lerpForwards(float dist, float current, float next)
    {
        float delta = dist * (next - current);
        if (delta < 0) return next;
        return current + delta;
    }

    public static int wrap(int value, int min, int max)
    {
        if (value >= min && value < max)
            return value;

        int t = value - min;
        int s = max - min + 1;

        if (s <= 0) return min;

        t %= s;
        if (t < 0) t += s;
        return min + t;
    }

    public static float wrapDegreesPositive(float degrees)
    {
        float f = degrees % 360.0F;

        if (f < 0)
            f += 360;

        return f;
    }

    public static double exactDistance(FluidNode node1, FluidNode node2)
    {
        Vec3 offset1 = new Vec3(node1.getNodePos().face().step()).scale(0.5);
        Vec3 v1 = Vec3.atCenterOf(node1.getNodePos().pos()).add(offset1);
        Vec3 offset2 = new Vec3(node2.getNodePos().face().step()).scale(0.5);
        Vec3 v2 = Vec3.atCenterOf(node2.getNodePos().pos()).add(offset2);
        return manhattanDistance(v1, v2);
    }

    /**
     * Converts a 3D vector to pitch and yaw in degrees, neglecting roll.
     */
    public static Vec2 rectToPol(double u, double v, double w)
    {
        float xz = (float) Math.sqrt(w * w + u * u);
        return new Vec2(
                (float) Math.toDegrees(Math.atan(-v / xz)), // Pitch
                (float) Math.toDegrees(Math.atan2(-u, w))); // Yaw
    }

    public static Vec2 rectToPol(Vec3 vec)
    {
        return rectToPol(vec.x, vec.y, vec.z);
    }

    public static float sin(long time, float tickDelta, float timeFactor)
    {
        // This one is mathematically flawed
//        float time1 = wrapDegrees(time);
//        return MathHelper.sin((time1 + tickDelta) * timeFactor);

        // This one has too many trig operations
        // And time * timeFactor becomes a float rather than a double, causing imprecision. This could be solved by adding casts.
//        return (float) (Math.sin(time * timeFactor) * MathHelper.cos(tickDelta * timeFactor) + Math.cos(time * timeFactor) * MathHelper.sin(tickDelta * timeFactor));

        // This one works, but the argument could exceed the maximum double value if time and factor are both massive (unlikely)
        return (float) Math.sin(((double) time + tickDelta) * timeFactor);

        // Something is definitely wrong with this, but I'm not sure what.
//        int shift = 1000000000;
//        long upper = (long) (MathHelper.TAU * shift);
//        float down = ((float) (time % upper)) / shift;
//        return MathHelper.sin((down + tickDelta) * timeFactor);
    }

    public static long wrapDegrees(long degrees)
    {
        long l = degrees % 360;
        if (l >= 180)
        {
            l -= 360;
        }

        if (l < -180)
        {
            l += 360;
        }

        return l;
    }

    public static int wrapIndex(int idx, int length)
    {
        idx %= length;

        if (idx < 0)
            return length + (idx % length);

        return idx;
    }

//    public static boolean boxIntersectsLine(Box box, Vec3d min, Vec3d max)
//    {
//        // Check if start point is within the box
//        boolean xStart = min.x >= box.minX && min.x <= box.maxX;
//        boolean yStart = min.y >= box.minY && min.y <= box.maxY;
//        boolean zStart = min.z >= box.minZ && min.z <= box.maxZ;
//
//        if (xStart && yStart && zStart)
//            return true;
//
//
//
//        return false;
//    }

    /// I'm not entirely sure what this does that is special.
    /// I suspect it returns true when min is within the box, unlike Box::raycast.
    @Nullable
    public static Vec3 raycast(AABB box, Vec3 min, Vec3 max)
    {
        double[] ds = new double[]{1.0};
        double d = max.x - min.x;
        double e = max.y - min.y;
        double f = max.z - min.z;
        Direction direction = BoxMixin.traceCollisionSide(box, min, ds, null, d, e, f);
        if (direction == null)
        {
            if (box.contains(min))
                return min;

            return null;
        }
        else
        {
            double g = ds[0];
            return min.add(g * d, g * e, g * f);
        }
    }

    public static Vec3 getRotationVector(double pitchRad, double yawRad)
    {
        float f = (float) pitchRad;
        float g = (float) -yawRad;
        float h = Mth.cos(g);
        float i = Mth.sin(g);
        float j = Mth.cos(f);
        float k = Mth.sin(f);
        return new Vec3(i * j, -k, h * j);
    }

    public static double distance2D(double x0, double y0, double x1, double y1)
    {
        double dx = x1 - x0;
        double dy = y1 - y0;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static float wrapRadians(float radians)
    {
        return radians - Mth.TWO_PI * Mth.floor((radians + Math.PI) / Mth.TWO_PI);

//        float f = radians % MathHelper.TAU;
//        if (f >= MathHelper.PI)
//        {
//            f -= MathHelper.TAU;
//        }
//
//        if (f < -MathHelper.PI)
//        {
//            f += MathHelper.TAU;
//        }
//
//        return f;
    }

    public static float moveTowards(float current, float target, float speed)
    {
        float difference = target - current;
        if (Math.abs(difference) > 0)
            return current + Math.signum(difference) * Math.min(speed, Math.abs(difference));

        return target;
    }

    public static float deltaAngle(float radians1, float radians2)
    {
        float diff = (radians2 - radians1 + Mth.PI) % Mth.TWO_PI - Mth.PI;
        return diff < -Mth.PI ? diff + Mth.TWO_PI : diff;
    }

    public static float moveTowardsAngle(float current, float target, float speed)
    {
        float difference = deltaAngle(current, target);
        if (Math.abs(difference) > Mth.EPSILON)
            return current + Math.signum(difference) * Math.min(speed, Math.abs(difference));

        return target;
    }

    public static float log10(float f)
    {
        return (float) Math.log10(f);
    }
}
