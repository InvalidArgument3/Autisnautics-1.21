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

package com.neep.neepmeat.entity.weapon_switching;

import com.neep.neepmeat.init.NMSounds;
import java.util.EnumSet;
import java.util.function.Supplier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class GunAttackGoal<T extends WeaponSwitchingHostileEntity> extends Goal
{
    private final T actor;
    private final Supplier<ShotProvider> shotProvider;
    private final double speed;
    private int attackInterval;
    private final float squaredRange;
    private int cooldown = -1;
    private int targetSeeingTicker;
    private boolean movingToLeft;
    private boolean backward;
    private int combatTicks = -1;

    private boolean aiming;
    private int timeAiming = 0;

    private int shotsRemaining = 0;

    public GunAttackGoal(T actor, Supplier<ShotProvider> shotProvider, double speed, float range)
    {
        this.actor = actor;
        this.shotProvider = shotProvider;
        this.speed = speed;
        this.squaredRange = range * range;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
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
        return shotProvider.get() != null;
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

        aiming = false;
        timeAiming = 0;
        actor.setAimProgress(0);
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

            // Reduce strafe speed when trying to aim
            float leftRight = movingToLeft ? 0.5f : -0.5f;
            if (aiming)
                leftRight *= 0.1f;
            else
                leftRight *= 0.5f;

            actor.getMoveControl().strafe(backward ? -0.5f : 0.5f, leftRight);
            actor.lookAt(target, 30.0f, 30.0f);
        }
        else
        {
            actor.getLookControl().setLookAt(target, 30.0f, 30.0f);
        }

        var gun = shotProvider.get();

        // Try to reload
        if (cooldown <= 0 && shotsRemaining <= 0)
        {
            cooldown = 60;
            shotsRemaining = gun.maxShots();
            aiming = false;
            timeAiming = 0;

            actor.playSound(NMSounds.RELOAD_MECHANICAL_HISS, 1, 1);
        }

        if (!canSee && aiming)
        {
            // Limit aiming after losing sight of the target
            // Prevents instantly shooting the player after they move out from cover.
            timeAiming = Math.min(timeAiming, 10);
        }

        if (aiming && cooldown <= 0)
        {
            timeAiming = Mth.clamp(timeAiming + 1, 0, 20); // Runs every tick
            if (!canSee && targetSeeingTicker < -60)
            {
                aiming = false;
                timeAiming = 0;
            }
            else if (canSee && timeAiming >= 20)
            {
                if (shotsRemaining > 0)
                {
                    actor.performRangedAttack(target, 1);
                    cooldown = gun.shotCooldown();
                    shotsRemaining--;
                }
            }
        }

        if (--cooldown <= 0 && targetSeeingTicker >= -60)
        {
            aiming = true;
        }

        // Synced to client, used to animate the arms
        actor.setAimProgress(timeAiming / 20f);
    }
}