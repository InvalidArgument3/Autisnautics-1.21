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

import com.neep.neepmeat.entity.weapon_switching.BetterMeleeGoal;
import com.neep.neepmeat.entity.weapon_switching.WeaponSwitchingHostileEntity;
import java.util.EnumSet;
import java.util.function.Supplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class KeeperCritGoal extends Goal
{
    protected int ticks;
    private final Supplier<BetterMeleeGoal> meleeGoal;
    protected float range;
    protected int cooldown;
    protected final WeaponSwitchingHostileEntity entity;

    public KeeperCritGoal(WeaponSwitchingHostileEntity entity, Supplier<BetterMeleeGoal> meleeGoal, float range)
    {
//        super(mob, speed, false);
        this.entity = entity;
        this.meleeGoal = meleeGoal;
        this.range = range;
        this.setFlags(EnumSet.of(Flag.JUMP));
    }

//    @Override
//    protected int getMaxCooldown()
//    {
//        return this.getTickCount(maxCooldown);
//    }

    @Override
    public boolean canUse()
    {
        return entity.getTarget() != null && entity.onGround();
    }

    @Override
    public void start()
    {
        super.start();
        this.ticks = 0;
        entity.setAggressive(false);
    }

    @Override
    public void stop()
    {
        super.stop();
    }

    @Override
    public void tick()
    {
        super.tick();
        ++this.ticks;

        LivingEntity target = entity.getTarget();
        cooldown = Math.max(cooldown - 1, 0);
        if (cooldown == 0 && target != null && entity.closerThan(target, range))
        {
            if (entity.distanceToSqr(target) < meleeGoal.get().getAttackReachSqr(target))
            {
                entity.getJumpControl().jump();
                entity.setAggressive(true);
                meleeGoal.get().scheduleCrit(10);
                cooldown = adjustedTickDelay(25);
            }
        }
    }
}
