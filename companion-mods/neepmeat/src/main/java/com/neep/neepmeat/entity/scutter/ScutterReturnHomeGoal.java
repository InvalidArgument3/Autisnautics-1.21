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

package com.neep.neepmeat.entity.scutter;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;

public class ScutterReturnHomeGoal extends MoveToBlockGoal
{
    private final FarmingScutter scutter;
    private boolean reached;

    public ScutterReturnHomeGoal(FarmingScutter scutter, float speed, int range)
    {
        super(scutter, speed, range);
        this.scutter = scutter;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse()
    {
        if (nextStartTick > 0)
        {
            nextStartTick--;
        }
        else if (scutter.getHomePos() != null && (scutter.needsEmptying() || scutter.getTargets().isEmpty()))
        {
            nextStartTick = nextStartTick(mob);
            blockPos = scutter.getHomePos();
            return true;
        }
        return false;
    }

    @Override
    public void tick()
    {
        BlockPos blockPos = this.getMoveToTarget();
        Vec3 mobPos = mob.position();
        if (!blockPos.closerToCenterThan(mobPos, this.acceptedDistance()))
        {
            this.reached = false;
            ++this.tryTicks;

            // Entity stops two blocks away from the target for unknown reasons
            if (blockPos.closerToCenterThan(mobPos, 2.1))
            {
                mob.push(
                        0.05 * (blockPos.getX() + 0.5 - mobPos.x),
                        0,
                        0.05 * (blockPos.getZ() + 0.5 - mobPos.z));
            }

            if (this.shouldRecalculatePath())
            {
                this.mob.getNavigation().moveTo(mob.getNavigation().createPath(
                                blockPos.getX() + 0.5,
                                blockPos.getY(),
                                blockPos.getZ() + 0.5,
                                0), this.speedModifier);
            }
        }
        else
        {
            this.reached = true;
            --this.tryTicks;
        }
    }

    protected boolean isReachedTarget()
    {
        return this.reached;
    }

    @Override
    public void start()
    {
        super.start();
    }

    @Override
    protected int nextStartTick(PathfinderMob mob)
    {
        return 20;
    }

    @Override
    public double acceptedDistance()
    {
        return 0.5;
    }

    @Override
    protected boolean isValidTarget(LevelReader world, BlockPos pos)
    {
        return pos.equals(scutter.getHomePos());
    }
}
