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

package com.neep.neepmeat.client.world;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

@OnlyIn(Dist.CLIENT)
public class DuatDimensionEffects extends DimensionSpecialEffects
{
    public DuatDimensionEffects()
    {
        super(Float.NaN, true, SkyType.NORMAL, false, true);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight)
    {
        return new Vec3(0, 0, 0);
    }

    @Override
    public boolean isFoggyAt(int camX, int camY)
    {
//        long t = MinecraftClient.getInstance().world.getTime();
//        int period = 100;
//        return t - Math.floor((double) t / period) * period < (double) period / 2;
        return false;
    }
}