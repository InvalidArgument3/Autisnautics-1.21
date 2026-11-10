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
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;

public class ScutterMoveToCropGoal extends MoveToBlockGoal
{
    private final FarmingScutter scutter;
    private boolean reached;

    public ScutterMoveToCropGoal(FarmingScutter scutter, double speed, int range)
    {
        super(scutter, speed, range);
        this.scutter = scutter;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse()
    {
        if (nextStartTick > 0 && scutter.getTargets().isEmpty())
        {
            nextStartTick--;
        }
        else if (!scutter.getTargets().isEmpty())
        {
            nextStartTick = nextStartTick(mob);
            blockPos = scutter.getTargets().iterator().next().below();
//            BlockState state = mob.getWorld().getBlockState(targetPos);
//            if (state
            return true;
        }
        return false;
    }

    @Override
    protected int nextStartTick(PathfinderMob mob)
    {
        return 10;
    }

    @Override
    public boolean canContinueToUse()
    {
        return super.canContinueToUse() && !isReachedTarget() && !scutter.getTargets().isEmpty();
    }

    @Override
    public void tick()
    {
        if (scutter.getTargets().isEmpty())
            return;

        if (isReachedTarget())
        {
            scutter.getTargets().remove(blockPos);
        }

        BlockPos blockPos = this.getMoveToTarget();
        double distance = blockPos.distToCenterSqr(mob.position());
        if (distance > acceptedDistance())
        {
            this.reached = false;
            this.tryTicks++;
            if (this.shouldRecalculatePath())
            {
                var path = mob.getNavigation().createPath((double) blockPos.getX() + 0.5, (double) blockPos.getY(), (double) blockPos.getZ() + 0.5, 0);
                mob.getNavigation().moveTo(path, speedModifier);
            }

            // Fix the stupid issue that happens with Kilt
            if (distance < 2.1)
            {
                var mobPos = mob.position();
                mob.lookAt(EntityAnchorArgument.Anchor.EYES, blockPos.getCenter());
                mob.push(
                        0.05 * (blockPos.getX() + 0.5 - mobPos.x),
                        0,
                        0.05 * (blockPos.getZ() + 0.5 - mobPos.z));
            }
        }
        else
        {
            this.reached = true;
            this.tryTicks--;
        }
    }

    @Override
    protected void moveMobToBlock()
    {
        // Super implementation uses distance=1 and targets getY + 1

        var path = mob.getNavigation().createPath(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5, 0);
        mob.getNavigation().moveTo(path, speedModifier);
    }

    @Override
    protected boolean isReachedTarget()
    {
        return this.reached;
    }

    @Override
    public double acceptedDistance()
    {
        return 0.1f;
    }

    @Override
    protected boolean findNearestBlock()
    {
        // Does nothing
        return true;
    }

    @Override
    protected boolean isValidTarget(LevelReader world, BlockPos pos)
    {
        return pos.equals(blockPos);
    }
}
