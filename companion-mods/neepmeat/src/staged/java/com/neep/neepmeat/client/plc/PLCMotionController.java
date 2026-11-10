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

package com.neep.neepmeat.client.plc;

import com.neep.neepmeat.machine.surgical_controller.SurgicalRobot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.util.Mth;

@Environment(value = Dist.CLIENT)
public class PLCMotionController
{
    private final SurgicalRobot robot;

    private final RotationMode rotationMode = RotationMode.FREE;

    public float lerpPitch;
    public float lerpYaw;

    public PLCMotionController(SurgicalRobot robot)
    {
        this.robot = robot;
        this.lerpPitch = robot.getPitch();
        this.lerpYaw = robot.getYaw();
    }

    public void update()
    {
        if (rotationMode == RotationMode.LOCKED)
        {
            robot.setPitchYaw(20, robot.getYaw());
        }

        lerpPitch = Mth.rotLerp(0.3f, lerpPitch,  robot.getPitch());
        lerpYaw = Mth.rotLerp(0.3f, lerpYaw,  robot.getYaw());
    }

    public void setPitchYaw(float pitch, float yaw)
    {
        robot.setPitchYaw(Mth.wrapDegrees(pitch), Mth.wrapDegrees(yaw));
    }

    public float getPitch()
    {
        return robot.getPitch();
    }

    public float getYaw()
    {
        return robot.getYaw();
    }

    enum RotationMode
    {
        LOCKED,
        FREE
    }
}
