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

package com.neep.neepmeat.entity.compressor_scutter;

import com.neep.neepmeat.entity.scutter.ScutterEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import java.util.EnumSet;

public class ScutterFollowOwnerGoal extends Goal
{
    private final ScutterEntity entity;
    private final LevelReader world;
    private final double speed;
    private final PathNavigation navigation;
    private final float maxDistance;
    private final float minDistance;
    private final boolean leavesAllowed;
    private LivingEntity owner;
    private int updateCountdownTicks;
    private float oldWaterPathfindingPenalty;
    private final float teleportDistance;

    public ScutterFollowOwnerGoal(ScutterEntity entity, double speed, float minDistance, float maxDistance, boolean leavesAllowed, float teleportDistance)
    {
        this.entity = entity;
        this.world = entity.level();
        this.speed = speed;
        this.navigation = entity.getNavigation();
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.leavesAllowed = leavesAllowed;
        this.teleportDistance = teleportDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse()
    {
        LivingEntity livingEntity = this.entity.getOwner();
        if (livingEntity == null)
        {
            return false;
        }
        else if (livingEntity.isSpectator())
        {
            return false;
        }
        else if (this.cannotFollow())
        {
            return false;
        }
        else if (this.entity.distanceToSqr(livingEntity) < (double) (this.minDistance * this.minDistance))
        {
            return false;
        }
        else
        {
            this.owner = livingEntity;
            return true;
        }
    }

    @Override
    public boolean canContinueToUse()
    {
        if (this.navigation.isDone())
        {
            return false;
        }
        else
        {
            return this.cannotFollow() ? false : !(this.entity.distanceToSqr(this.owner) <= (double) (this.maxDistance * this.maxDistance));
        }
    }

    private boolean cannotFollow()
    {
        return this.entity.isStatic() || this.entity.isPassenger() || this.entity.isLeashed();
    }

    @Override
    public void start()
    {
        this.updateCountdownTicks = 0;
        this.oldWaterPathfindingPenalty = this.entity.getPathfindingMalus(BlockPathTypes.WATER);
        this.entity.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
    }

    @Override
    public void stop()
    {
        this.owner = null;
        this.navigation.stop();
        this.entity.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterPathfindingPenalty);
    }

    @Override
    public void tick()
    {
        this.entity.getLookControl().setLookAt(this.owner, 10.0F, (float) this.entity.getMaxHeadXRot());
        if (--this.updateCountdownTicks <= 0)
        {
            this.updateCountdownTicks = this.adjustedTickDelay(10);
            if (this.entity.distanceTo(this.owner) >= teleportDistance)
            {
                this.tryTeleport();
            }
            else
            {
                this.navigation.moveTo(this.owner, this.speed);
            }
        }
    }

    private void tryTeleport()
    {
        BlockPos blockPos = this.owner.blockPosition();

        for (int i = 0; i < 10; i++)
        {
            int j = this.getRandomInt(-3, 3);
            int k = this.getRandomInt(-1, 1);
            int l = this.getRandomInt(-3, 3);

            if (tryTeleportTo(blockPos.getX() + j, blockPos.getY() + k, blockPos.getZ() + l))
            {
                return;
            }
        }
    }

    private boolean tryTeleportTo(int x, int y, int z)
    {
        if (Math.abs((double) x - this.owner.getX()) < 2.0 && Math.abs((double) z - this.owner.getZ()) < 2.0)
        {
            return false;
        }
        else if (!this.canTeleportTo(new BlockPos(x, y, z)))
        {
            return false;
        }
        else
        {
            this.entity.moveTo((double) x + 0.5, (double) y, (double) z + 0.5, this.entity.getYRot(), this.entity.getXRot());
            this.navigation.stop();
            return true;
        }
    }

    private boolean canTeleportTo(BlockPos pos)
    {
        BlockPathTypes pathNodeType = WalkNodeEvaluator.getBlockPathTypeStatic(this.world, pos.mutable());
        if (pathNodeType != BlockPathTypes.WALKABLE)
        {
            return false;
        }
        else
        {
            BlockState blockState = this.world.getBlockState(pos.below());
            if (!this.leavesAllowed && blockState.getBlock() instanceof LeavesBlock)
            {
                return false;
            }
            else
            {
                BlockPos blockPos = pos.subtract(this.entity.blockPosition());
                return this.world.noCollision(this.entity, this.entity.getBoundingBox().move(blockPos));
            }
        }
    }

    private int getRandomInt(int min, int max)
    {
        return this.entity.getRandom().nextInt(max - min + 1) + min;
    }
}