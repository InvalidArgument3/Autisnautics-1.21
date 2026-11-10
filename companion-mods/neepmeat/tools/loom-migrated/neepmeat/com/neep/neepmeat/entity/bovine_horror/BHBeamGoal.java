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

import com.neep.meatweapons.Util;
import com.neep.meatweapons.particle.MWGraphicsEffects;
import com.neep.meatweapons.particle.MWParticles;
import com.neep.neepmeat.entity.goal.AnimatedGoal;
import com.neep.neepmeat.init.NMSounds;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

public class BHBeamGoal extends AnimatedGoal<BHBeamGoal>
{
    private final BovineHorrorEntity mob;

    protected final Sequence<BHBeamGoal> orient = (action, counter) ->
    {
        var mob = action.mob;

        if (mob.getTarget() == null)
        {
            markFinished();
            return;
        }

        if (counter == 0)
        {
            mob.syncNearby("animation.horror.beam");
            mob.playSound(NMSounds.BH_CHARGE, 1, 0.9f);
            mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(), NMSounds.BH_PHASE2, SoundSource.HOSTILE, 3, 1);
        }

        mob.setVisibility(1);

        if (counter > 10 && counter % 2 == 0)
        {
            if (mob.level() instanceof ServerLevel serverWorld)
            {
                Vec3 origin = getOrigin();
                serverWorld.sendParticles(MWParticles.PLASMA_PARTICLE, origin.x, origin.y, origin.z,
                        10, 1, 1, 1, 1);
            }
        }

        if (counter > 40)
        {
            setSequence(action.shootBeams);
        }
    };

    protected final Sequence<BHBeamGoal> shootBeams = (action, counter) ->
    {
        LivingEntity target = action.mob.getTarget();
        if (target != null && counter < 20 && counter % 2 == 0)
        {
            Vec3 origin = getOrigin();
            Vec3 toEnd = target.position().subtract(origin).scale(25);
            var random = action.mob.getRandom();
            Vec3 end = origin.add(
                    toEnd.x + random.triangle(0, 20),
                    toEnd.y + random.triangle(0, 20) + target.getBbHeight() / 2,
                    toEnd.z + random.triangle(0, 20)
            );
            shootBeam(origin, end);
        }
        if (counter > 40)
        {
            markFinished();
        }
    };

    protected Vec3 getOrigin()
    {
        return getMob().position().add(0, 4, 0);
    }

    protected void shootBeam(Vec3 origin, Vec3 end)
    {
        var foundTarget = hitScan(mob, origin, end, 30);
        DamageSource damageSource = mob.level().damageSources().mobAttack(mob);

        if (foundTarget.first() != null)
        {
            foundTarget.first().hurt(damageSource, 8);
        }

        PlayerLookup.tracking(getMob()).forEach(p ->
        {
            MWGraphicsEffects.syncBeamEffect(p, MWGraphicsEffects.BEAM, getMob().level(),
                    origin, foundTarget.second(), Vec3.ZERO, 1f, 20);
        });

    }

    protected Pair<Entity, Vec3> hitScan(LivingEntity caster, Vec3 start, Vec3 end, double distance)
    {
        Level world = caster.level();

        // Find where the ray hits a block
        ClipContext ctx = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster);
        BlockHitResult blockResult = world.clip(ctx);

        Predicate<Entity> entityFilter = entity -> !entity.isSpectator() && entity.isPickable();

        double minDistance = distance;
        Entity entity = null;
        EntityHitResult entityResult = null;
        for (var it = Util.getRayTargets(caster, caster.getBoundingBox(), start, blockResult.getLocation(), entityFilter, 0.1).iterator(); it.hasNext(); )
        {
            EntityHitResult result = it.next();
            if (result.getLocation().distanceTo(start) < minDistance)
            {
                minDistance = result.getLocation().distanceTo(start);
                entity = result.getEntity();
                entityResult = result;
            }
        }

        Vec3 hitPos = Objects.requireNonNullElse(entityResult, blockResult).getLocation();

        return Pair.of(entity, hitPos);
    }

    public BHBeamGoal(BovineHorrorEntity mob)
    {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse()
    {
        return mob.getTarget() != null && mob.getTarget().isAlive();
    }

    @Override
    public boolean canContinueToUse()
    {
        return super.canContinueToUse() && canUse();
    }

    @Override
    public void start()
    {
        super.start();
        setSequence(orient);
    }

    protected BovineHorrorEntity getMob()
    {
        return mob;
    }
}
