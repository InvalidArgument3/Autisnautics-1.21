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

package com.neep.neepmeat.entity.worm;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

public class WormTargetGoal extends Goal
{
    protected final WormEntity parent;
    protected LivingEntity target;
    protected final TargetingConditions targetPredicate;
    protected final float range;

    public WormTargetGoal(WormEntity parent, TargetingConditions targetPredicate, float range)
    {
        this.parent = parent;
        this.targetPredicate = targetPredicate;
        this.range = range;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse()
    {
        return parent.getTarget() == null;
    }

    @Override
    public void start()
    {
        target = findTarget(getRange());
    }

    protected LivingEntity findTarget(float range)
    {
        Vec3 pos = parent.position();
        AABB box = AABB.ofSize(pos, range * 2, range * 2, range * 2);
        List<LivingEntity> el = parent.level().getEntitiesOfClass(LivingEntity.class, box, e -> true);
        return parent.level().getNearestEntity(LivingEntity.class, targetPredicate, parent, pos.x, pos.y, pos.z, box);
    }

    @Override
    public boolean canContinueToUse()
    {
        LivingEntity entity = parent.getTarget();
        if (entity == null) entity = this.target;

        if (entity == null || entity.isRemoved() || !targetPredicate.test(parent, entity)) return false;

        if (!parent.canAttack(entity))
        {
            return false;
        }
        Team abstractTeam = parent.getTeam();
        Team abstractTeam2 = entity.getTeam();
        if (abstractTeam != null && abstractTeam2 == abstractTeam)
        {
            return false;
        }
        double d = this.getRange();
        if (parent.distanceToSqr(entity) > d * d)
        {
            return false;
        }

        parent.setTarget(entity);
        return true;
    }

    private float getRange()
    {
        return range;
    }

    @Override
    public void stop()
    {
        parent.setTarget(null);
        this.target = null;
    }

    @Override
    public EnumSet<Flag> getFlags()
    {
        return super.getFlags();
    }
}
