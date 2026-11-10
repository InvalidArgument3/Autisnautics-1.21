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

import com.neep.neepmeat.init.NMEntities;
import com.neep.neepmeat.init.NMParticles;
import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.network.ParticleSpawnS2C;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BHAcidAttackGoal extends Goal
{
    protected BovineHorrorEntity mob;
    protected int ticks = 0;
    protected int spawned = 0;
    protected int maxSpawned;
    protected int cooldown = 0;

    public BHAcidAttackGoal(BovineHorrorEntity mob, float size, int maxSpawned)
    {
        this.mob = mob;
        this.maxSpawned = maxSpawned;
//        this.setControls(EnumSet.of(Control.MOVE));
    }
    @Override
    public boolean canUse()
    {
        LivingEntity target = mob.getTarget();
        if (target != null && target.isAlive())
        {
            return !mob.canMelee(target);
//            double dist = mob.getTarget().getPos().distanceTo(mob.getPos());
//            return dist >= 7;
        }
        return false;
    }

    @Override
    public void start()
    {
        ticks = 0;
        spawned = 0;
        cooldown = 0;
    }

    @Override
    public boolean canContinueToUse()
    {
        return (mob.getTarget() != null) && spawned < maxSpawned;
    }

    @Override
    public boolean isInterruptable()
    {
        return ticks <= 10 || ticks >= 50;
    }

    @Override
    public void stop()
    {
    }


    @Override
    public void tick()
    {
        super.tick();
        ticks += adjustedTickDelay(1);
        cooldown -= adjustedTickDelay(1);

        Level world = mob.level();

        LivingEntity target = mob.getTarget();
        if (ticks > 30 && ticks < 50 && (world.getGameTime() & 2) == 0)
        {
//            Vec3d toTarget = new Vec3d(Math.sin(mob.getYaw()), 0.1, Math.cos(mob.getY()));
           Vec3 toTarget = new Vec3(0, 0.1, 0);
            Vec3 spread = new Vec3(1, 1, 1);
            Vec3 origin = new Vec3(mob.getX(), mob.getY() + 3, mob.getZ());

            for (ServerPlayer player : PlayerLookup.tracking(mob))
            {
                ParticleSpawnS2C.send(player, NMParticles.BODY_COMPOUND_SHOWER, origin, toTarget, spread, 10);
            }
        }

        if (ticks > 30 && spawned < maxSpawned && cooldown <= 0)
        {
            Vec3 toTarget = target.position().subtract(mob.position());

            var projectile = NMEntities.ACID_SPRAY.create(world);
            float speed = toTarget.length() > 20 ? 2 : 0.5f;
            projectile.target(new Vec3(mob.getX(), mob.getY() + 4, mob.getZ()),
                    target.position(), speed, 1);
            projectile.setOwner(mob);
            world.addFreshEntity(projectile);
            mob.playSound(NMSounds.BH_SPIT, 4, 0.9f);

            cooldown = 6;
            spawned++;
        }
    }
}
