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

package com.neep.neepmeat.entity.keeper;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class KeeperRetreatGoal extends Goal
{
    protected final KeeperEntity entity;
    protected final double range;
    private final double fastSpeed;

    protected final PathNavigation fleeingEntityNavigation;
    private Path fleePath;

    public KeeperRetreatGoal(KeeperEntity entity, float speed, int range)
    {
        this.entity = entity;
        this.range = range;
        this.fastSpeed = speed;
        this.fleeingEntityNavigation = entity.getNavigation();
    }

    @Override
    public boolean canUse()
    {
        if (entity.getLastHurtByMob() == null) return false;

        Vec3 vec3d = DefaultRandomPos.getPosAway(entity, 16, 7, entity.getLastHurtByMob().position());
        if (vec3d == null)
        {
            return false;
        }

        this.fleePath = this.fleeingEntityNavigation.createPath(vec3d.x, vec3d.y, vec3d.z, 0);
        return entity.shouldHeal();
    }


    @Override
    public void start()
    {
        this.fleeingEntityNavigation.moveTo(this.fleePath, fastSpeed);
    }

    @Override
    public boolean canContinueToUse()
    {
        return entity.getLastHurtByMob() != null && !this.fleeingEntityNavigation.isDone();
    }

    @Override
    public void stop()
    {
        super.stop();
    }

//    protected void updatePos()
//    {
//        double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * range;
//        double y = entity.getY() + (entity.getRandom().nextDouble() - 0.5) * range;
//        double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * range;
//
//        foundPos = targetPos != null;
//        targetPos = getValidPos(x, y, z);
//    }
//
//    protected BlockPos getValidPos(double x, double y, double z)
//    {
//        BlockPos.Mutable mutable = new BlockPos.Mutable(x, y, z);
//
//        while (mutable.getY() > entity.world.getBottomY() && !entity.world.getBlockState(mutable).getMaterial().blocksMovement())
//        {
//            mutable.move(Direction.DOWN);
//        }
//
//        BlockState blockState = entity.world.getBlockState(mutable);
//        if (blockState.getMaterial().blocksMovement()) return  null;
//
//        return new BlockPos(x, y, z);
//    }

    @Override
    public void tick()
    {
        if (entity.distanceToSqr(entity.getLastHurtByMob()) < 49.0)
        {
            entity.getNavigation().setSpeedModifier(this.fastSpeed);
        }
    }
}
