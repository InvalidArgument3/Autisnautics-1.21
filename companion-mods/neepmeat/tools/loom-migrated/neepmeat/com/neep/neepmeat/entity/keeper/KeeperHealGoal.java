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

import com.neep.neepmeat.init.NMSounds;
import java.util.EnumSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;

public class KeeperHealGoal extends Goal
{
    protected int healTime;
    protected float range;
    protected int ticks;
    protected final KeeperEntity entity;
    protected final int maxTicks = 20;

    public KeeperHealGoal(KeeperEntity entity, float range, int healTime)
    {
        this.entity = entity;
        this.range = range;
        this.healTime = healTime;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse()
    {
        return
                (entity.getLastHurtByMob() != null && entity.distanceTo(entity.getLastHurtByMob()) > range && entity.shouldHeal())
                || (entity.getLastHurtByMob() == null && entity.getTarget() == null && entity.getHealth() < entity.getMaxHealth());
    }

    @Override
    public void start()
    {
        super.start();
        ticks = 0;
    }

    @Override
    public void tick()
    {
        super.tick();
        entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);
        ++ticks;
    }

    @Override
    public boolean canContinueToUse()
    {
        return ticks < healTime;
    }

    @Override
    public void stop()
    {
        super.stop();
        if (ticks >= adjustedTickDelay(healTime))
        {
            if (entity.level() instanceof ServerLevel serverWorld)
            {
                serverWorld.sendParticles(ParticleTypes.ENTITY_EFFECT, entity.getX(), entity.getY() + 1, entity.getZ(), 30, 0.4, 1.7, 0.4, 0.1);
                serverWorld.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), NMSounds.COMPOUND_INJECTOR, SoundSource.HOSTILE, 1f, 1f, true);
                entity.setHealth(Mth.clamp(0, entity.getHealth() + 8, entity.getMaxHealth()));
            }
        }
    }
}
