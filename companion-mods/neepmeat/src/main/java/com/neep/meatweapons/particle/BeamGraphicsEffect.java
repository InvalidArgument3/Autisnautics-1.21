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

package com.neep.meatweapons.particle;

import com.neep.meatlib.graphics.GraphicsEffect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class BeamGraphicsEffect implements GraphicsEffect
{
    protected Level world;
    protected long time;
    public boolean alive = true;
    protected Vec3 start;
    protected Vec3 end;
    protected Vec3 col;
    protected int maxTime;
    protected float scale;

    public BeamGraphicsEffect(Level world, Vec3 start, Vec3 end, Vec3 col, float scale, int maxTime, FriendlyByteBuf buf)
    {
        this.world = world;
        this.start = start;
        this.end = end;
        this.col = col;
        this.scale = scale;
        this.maxTime = maxTime;
    }

    @Override
    public Level getWorld()
    {
        return world;
    }

    @Override
    public void tick()
    {
        if (!this.alive)
            return;

        if (maxTime > 0 && time > maxTime)
        {
            this.remove();
        }

        ++time;
    }

    @Override
    public boolean isRemoved()
    {
        return !alive;
    }

    public void remove()
    {
        this.alive = false;
    }


    @FunctionalInterface
    public interface Factory
    {
        BeamGraphicsEffect create(Level world, Vec3 start, Vec3 end, Vec3 velocity, float scale, int maxTime);
    }
}
