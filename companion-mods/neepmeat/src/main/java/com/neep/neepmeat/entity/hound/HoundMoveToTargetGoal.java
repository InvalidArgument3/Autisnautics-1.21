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

package com.neep.neepmeat.entity.hound;

import java.util.EnumSet;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;

public class HoundMoveToTargetGoal extends Goal
{
    protected final HoundEntity mob;
    private final double speed;
    private final boolean pauseWhenMobIdle;
    private Path path;
    private double targetX;
    private double targetY;
    private double targetZ;
    private int updateCountdownTicks;
    private int cooldown;
    private long lastUpdateTime;

    public HoundMoveToTargetGoal(HoundEntity mob, double speed, boolean pauseWhenMobIdle)
    {
        this.mob = mob;
        this.speed = speed;
        this.pauseWhenMobIdle = pauseWhenMobIdle;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse()
    {
        long worldTime = this.mob.level().getGameTime();
        if (worldTime - this.lastUpdateTime < 20L)
            return false;

        this.lastUpdateTime = worldTime;

        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive() || mob.closerThan(target, 1))
            return false;

        this.path = this.mob.getNavigation().createPath(target, 0);
        if (this.path != null)
            return true;

        return this.getSquaredMaxAttackDistance(target) >= this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
    }

    @Override
    public boolean canContinueToUse()
    {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive() || mob.closerThan(target, 1))
            return false;

        if (!this.pauseWhenMobIdle)
            return !this.mob.getNavigation().isDone();

        if (!this.mob.isWithinRestriction(target.blockPosition()))
            return false;

        return !(target instanceof Player) || !target.isSpectator() && !((Player)target).isCreative();
    }

    @Override
    public void start()
    {
        this.mob.getNavigation().moveTo(this.path, this.speed);
        this.mob.setAggressive(true);
        this.updateCountdownTicks = 0;
        this.cooldown = 0;
    }

    @Override
    public void stop()
    {
        LivingEntity livingEntity = this.mob.getTarget();
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingEntity))
        {
            this.mob.setTarget(null);
        }
        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick()
    {
        return true;
    }

    @Override
    public void tick()
    {
       LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity == null)
        {
            return;
        }
        this.mob.getLookControl().setLookAt(livingEntity, 30.0f, 30.0f);
        double d = this.mob.distanceToSqr(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
        this.updateCountdownTicks = Math.max(this.updateCountdownTicks - 1, 0);
        if ((this.pauseWhenMobIdle || this.mob.getSensing().hasLineOfSight(livingEntity))
                && this.updateCountdownTicks <= 0
                && (this.targetX == 0.0 && this.targetY == 0.0 && this.targetZ == 0.0 || livingEntity.distanceToSqr(this.targetX, this.targetY, this.targetZ) >= 1.0 || this.mob.getRandom().nextFloat() < 0.05f))
        {
            this.targetX = livingEntity.getX();
            this.targetY = livingEntity.getY();
            this.targetZ = livingEntity.getZ();
            this.updateCountdownTicks = 4 + this.mob.getRandom().nextInt(7);
            if (d > 1024.0)
            {
                this.updateCountdownTicks += 10;
            }
            else if (d > 256.0)
            {
                this.updateCountdownTicks += 5;
            }
            if (!this.mob.getNavigation().moveTo(livingEntity, this.speed))
            {
                this.updateCountdownTicks += 15;
            }
            this.updateCountdownTicks = this.adjustedTickDelay(this.updateCountdownTicks);
        }
        this.cooldown = Math.max(this.cooldown - 1, 0);
    }

    protected void resetCooldown()
    {
        this.cooldown = this.adjustedTickDelay(20);
    }

    protected double getSquaredMaxAttackDistance(LivingEntity entity)
    {
        return this.mob.getBbWidth() * 2.0f * (this.mob.getBbWidth() * 2.0f) + entity.getBbWidth();
    }
}
