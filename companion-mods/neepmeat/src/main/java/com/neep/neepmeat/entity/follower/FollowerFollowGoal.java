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

package com.neep.neepmeat.entity.follower;

import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;

public class FollowerFollowGoal extends Goal
{
    private final Mob mob;
    private final double speed;
    private final PathNavigation navigation;
    private final float minDistance;
    @Nullable private Entity target;
    private int updateCountdownTicks;
    private float oldWaterPathFindingPenalty;

    public FollowerFollowGoal(Mob mob, double speed, float minDistance)
    {
        this.mob = mob;
        this.speed = speed;
        this.navigation = mob.getNavigation();
        this.minDistance = minDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        if (!(mob.getNavigation() instanceof GroundPathNavigation) && !(mob.getNavigation() instanceof FlyingPathNavigation))
        {
            throw new IllegalArgumentException("Unsupported mob type for FollowMobGoal");
        }
    }

    @Override
    public boolean canUse()
    {
        Entity mobTarget = mob.getTarget();
        if (mobTarget != null && !mobTarget.isInvisible())
        {
            this.target = mobTarget;
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse()
    {
        return this.target != null && !this.navigation.isDone() && this.mob.distanceToSqr(this.target) > (double) (this.minDistance * this.minDistance);
    }

    @Override
    public void start()
    {
        this.updateCountdownTicks = 0;
        this.oldWaterPathFindingPenalty = this.mob.getPathfindingMalus(PathType.WATER);
        this.mob.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Override
    public void stop()
    {
        this.target = null;
        this.navigation.stop();
        this.mob.setPathfindingMalus(PathType.WATER, this.oldWaterPathFindingPenalty);
    }

    @Override
    public void tick()
    {
        if (this.target != null && !this.mob.isLeashed())
        {
            this.mob.getLookControl().setLookAt(this.target, 10.0F, (float) this.mob.getMaxHeadXRot());
            if (--this.updateCountdownTicks <= 0)
            {
                this.updateCountdownTicks = this.adjustedTickDelay(10);
                double d = this.mob.getX() - this.target.getX();
                double e = this.mob.getY() - this.target.getY();
                double f = this.mob.getZ() - this.target.getZ();
                double g = d * d + e * e + f * f;
                if (!(g <= (double) (this.minDistance * this.minDistance)))
                {
                    this.navigation.moveTo(this.target, this.speed);
                }
                else
                {
                    this.navigation.stop();
                    double h = this.target.getX() - this.mob.getX();
                    double i = this.target.getZ() - this.mob.getZ();
                    this.navigation.moveTo(this.mob.getX() - h, this.mob.getY(), this.mob.getZ() - i, this.speed);
                }
            }
        }
    }
}
