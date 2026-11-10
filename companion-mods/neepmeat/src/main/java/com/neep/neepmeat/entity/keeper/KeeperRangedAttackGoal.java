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

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;

public class KeeperRangedAttackGoal<T extends Monster & RangedAttackMob> extends Goal
{
    private final T actor;
    private final double speed;
    private int attackInterval;
    private final float squaredRange;
    private int cooldown = -1;
    private int targetSeeingTicker;
    private boolean movingToLeft;
    private boolean backward;
    private int combatTicks = -1;
    protected Item item;

    public KeeperRangedAttackGoal(T actor, double speed, int attackInterval, float range, Item item)
    {
        this.actor = actor;
        this.speed = speed;
        this.attackInterval = attackInterval;
        this.squaredRange = range * range;
        this.item = item;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public void setAttackInterval(int attackInterval)
    {
        this.attackInterval = attackInterval;
    }

    @Override
    public boolean canUse()
    {
        if (actor.getTarget() == null)
        {
            return false;
        }
        return this.isHoldingItem();
    }

    protected boolean isHoldingItem()
    {
        return actor.isHolding(item);
//        return actor.isHolding(stack -> stack.getItem() instanceof IGunItem);
    }

    @Override
    public boolean canContinueToUse()
    {
        return (this.canUse() || !actor.getNavigation().isDone()) && isHoldingItem();
    }

    @Override
    public void start()
    {
        super.start();
        actor.setAggressive(true);
    }

    @Override
    public void stop()
    {
        super.stop();
        actor.setAggressive(false);
        targetSeeingTicker = 0;
        cooldown = -1;
        actor.stopUsingItem();
    }

    @Override
    public boolean requiresUpdateEveryTick()
    {
        return true;
    }

    @Override
    public void tick()
    {
        LivingEntity target = actor.getTarget();

        if (target == null) return;

        double dist = actor.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean canSee = actor.getSensing().hasLineOfSight(target);
        boolean seen = targetSeeingTicker > 0;

        if (canSee != seen)
        {
            targetSeeingTicker = 0;
        }

        if (canSee)
        {
            ++targetSeeingTicker;
        }
        else
        {
            --targetSeeingTicker;
        }

        if (dist > squaredRange || targetSeeingTicker < 20)
        {
            actor.getNavigation().moveTo(target, speed);
            combatTicks = -1;
        }
        else
        {
            actor.getNavigation().stop();
            ++combatTicks;
        }

        if (combatTicks >= 20)
        {
            if (actor.getRandom().nextFloat() < 0.3)
            {
                movingToLeft = !movingToLeft;
            }
            if (actor.getRandom().nextFloat() < 0.3)
            {
                backward = !backward;
            }
            combatTicks = 0;
        }

        if (combatTicks > -1)
        {
            if (dist > squaredRange * 0.75f)
            {
                backward = false;
            }
            else if (dist < squaredRange * 0.25f)
            {
                backward = true;
            }
            actor.getMoveControl().strafe(backward ? -0.5f : 0.5f, movingToLeft ? 0.5f : -0.5f);
            actor.lookAt(target, 30.0f, 30.0f);
        }
        else
        {
            actor.getLookControl().setLookAt(target, 30.0f, 30.0f);
        }

        if (actor.isUsingItem())
        {
            int i;
            if (!canSee && targetSeeingTicker < -60)
            {
                actor.stopUsingItem();
            }
            else if (canSee && (i = actor.getTicksUsingItem()) >= 20)
            {
                actor.stopUsingItem();
                actor.performRangedAttack(target, BowItem.getPowerForTime(i));
                cooldown = attackInterval;
            }
        }
        else if (--cooldown <= 0 && targetSeeingTicker >= -60)
        {
            actor.startUsingItem(ProjectileUtil.getWeaponHoldingHand(actor, item));
        }
    }
}