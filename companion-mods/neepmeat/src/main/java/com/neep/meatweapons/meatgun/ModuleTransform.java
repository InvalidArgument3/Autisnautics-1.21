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

package com.neep.meatweapons.meatgun;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class ModuleTransform
{
    public final Matrix4f parentTransform = new Matrix4f(); // Parent relative to root origin
    public final Matrix4f globalTransform = new Matrix4f(); // Relative to root origin
    public final Matrix4f transform = new Matrix4f(); // Relative to parent origin

    public void set(Matrix4f newTransform)
    {
        transform.set(newTransform);
        globalTransform.set(parentTransform).mul(transform);
    }

    public void set(float[] fs)
    {
        transform.set(fs);
        globalTransform.set(parentTransform).mul(transform);
    }

    public void reset()
    {
        transform.identity();
    }

    public void setParent(@Nullable Matrix4f fullTransform)
    {
        if (fullTransform != null)
        {
            parentTransform.set(fullTransform);
            this.globalTransform.set(parentTransform).mul(transform);
        }
    }
}
