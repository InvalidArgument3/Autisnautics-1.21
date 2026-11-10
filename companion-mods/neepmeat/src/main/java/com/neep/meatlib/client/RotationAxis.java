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

package com.neep.meatlib.client;

// Presents an interface that does not change between 1.19.2 and 1.20.1

//? ver3 {

import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface RotationAxis
{
    RotationAxis NEGATIVE_X = rad -> new Quaternionf().rotationX(-rad);
    RotationAxis POSITIVE_X = rad -> new Quaternionf().rotationX(rad);
    RotationAxis NEGATIVE_Y = rad -> new Quaternionf().rotationY(-rad);
    RotationAxis POSITIVE_Y = rad -> new Quaternionf().rotationY(rad);
    RotationAxis NEGATIVE_Z = rad -> new Quaternionf().rotationZ(-rad);
    RotationAxis POSITIVE_Z = rad -> new Quaternionf().rotationZ(rad);

    static RotationAxis of(Vector3f axis)
    {
        return rad -> new Quaternionf().rotationAxis(rad, axis);
    }

    Quaternionf rotation(float rad);

    default Quaternionf rotationDegrees(float deg)
    {
        return this.rotation(deg * (float) (Math.PI / 180.0));
    }
}
//?} else {

/*import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

public interface RotationAxis
{
    RotationAxis NEGATIVE_X = rad -> Vec3f.NEGATIVE_X.getRadialQuaternion(rad);
    RotationAxis POSITIVE_X = rad -> Vec3f.POSITIVE_X.getRadialQuaternion(rad);
    RotationAxis NEGATIVE_Y = rad -> Vec3f.NEGATIVE_Y.getRadialQuaternion(rad);
    RotationAxis POSITIVE_Y = rad -> Vec3f.POSITIVE_Y.getRadialQuaternion(rad);
    RotationAxis NEGATIVE_Z = rad -> Vec3f.NEGATIVE_Z.getRadialQuaternion(rad);
    RotationAxis POSITIVE_Z = rad -> Vec3f.POSITIVE_Z.getRadialQuaternion(rad);

    static RotationAxis of(Vec3f axis)
    {
        return axis::getRadialQuaternion;
    }

    Quaternion rotation(float rad);

    default Quaternion rotationDegrees(float deg)
    {
        return this.rotation(deg * (float) (Math.PI / 180.0));
    }
}
*///?}
