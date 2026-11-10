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

package com.neep.neepmeat.entity.bovine_horror;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class BHMeleeAttackGoal extends Goal
{
    protected BovineHorrorEntity mob;
    protected int cooldown;
    private int maxCooldown = 20;

    public BHMeleeAttackGoal(BovineHorrorEntity mob)
    {
//        this.setControls(EnumSet.of(Control.MOVE));
        this.mob = mob;
    }

    @Override
    public boolean canUse()
    {
        return
                mob.getTarget() != null
                && mob.canMelee(mob.getTarget());
    }

    @Override
    public boolean canContinueToUse()
    {
        return canUse();
    }

    @Override
    public void start()
    {
        super.start();
        this.mob.setAggressive(true);
    }

    @Override
    public void stop()
    {
       super.stop();
        this.mob.setAggressive(false);
    }

    @Override
    public boolean requiresUpdateEveryTick()
    {
        return true;
    }

    @Override
    public void tick()
    {
        super.tick();
        if (mob.getTarget() != null)
        {
            attack(mob.getTarget());
        }
        cooldown--;
    }

    protected void attack(LivingEntity target)
    {
        if (mob.canMelee(target) && this.cooldown <= 0)
        {
            this.resetCooldown();
            this.mob.swing(InteractionHand.MAIN_HAND);
            this.mob.doHurtTarget(target);
        }
    }

    private void resetCooldown()
    {
        cooldown = maxCooldown;
    }
}
