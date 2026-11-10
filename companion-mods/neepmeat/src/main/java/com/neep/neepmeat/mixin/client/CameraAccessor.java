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

package com.neep.neepmeat.mixin.client;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraAccessor
{
    @Invoker void callSetPos(double x, double y, double z);
    @Invoker void callSetRotation(float yaw, float pitch);

    @Accessor void setThirdPerson(boolean thirdPerson);

    @Accessor("lastCameraY") float getLastCameraY();
    @Accessor("lastCameraY") void setLastCameraY(float lastY);
    @Accessor("cameraY") float getCameraY();
    @Accessor("cameraY") void setCameraY(float y);
}
