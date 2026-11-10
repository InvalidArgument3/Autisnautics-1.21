/*
 * Copyright (c) MeatWheeze 2026.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.meatweapons.entity;

import com.neep.meatweapons.item.BaseGunItem;
import com.neep.meatweapons.item.BeamEffectProvider;
import com.neep.meatweapons.meatgun.module.ShooterModule;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class HitScan
{
    private int maxRicochets = 0; // 0 means instant bye
    private float ricochetChance = 1;
    private float damage = 1;
    private float punch = 0;
    private float distance;
    private float margin = 0.1f;

//    private DamageHandler handler = (w, a, r) ->
//            r.getEntity().damage(BulletDamageSource.create(w, a, punch, false), damage);

    private BeamEffectProvider beamEffect = (world, pos, end, width, maxTime, showRadius, full) -> {};
    private int maxPierce;

    public HitScan(float distance)
    {
        this.distance = distance;
    }

    public HitScan ricochets(int ricochets)
    {
        this.maxRicochets = ricochets;
        return this;
    }

    public HitScan ricochetChance(float chance)
    {
        this.ricochetChance = chance;
        return this;
    }

    public HitScan damage(float damage)
    {
        this.damage = damage;
        return this;
    }

    public HitScan distance(float distance)
    {
        this.distance = distance;
        return this;
    }

    public HitScan margin(float margin)
    {
        this.margin = margin;
        return this;
    }

//    public HitScan damageHandler(DamageHandler handler)
//    {
//        this.handler = handler;
//        return this;
//    }

    public HitScan beamEffect(BeamEffectProvider beamEffect)
    {
        this.beamEffect = beamEffect;
        return this;
    }

    public HitScan maxPierce(int maxPierce)
    {
        this.maxPierce = maxPierce;
        return this;
    }

    public Result apply(Level world, LivingEntity shooter, ShooterModule.ShotOrigin origin, RandomSource random)
    {
        List<HitScanResult> entityHits = new ObjectArrayList<>();
        AABB originBox = new AABB(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5);

        // Distance is limited by the number of entities pierced before the block hit.

        // Set up ricochet loop
        int blockHits = 0;
        int ricochets = 0;
        int piercedEntities = 0;

        Vec3 finalPos = Vec3.ZERO;
        Vec3 rayOrigin = origin.pos();
        Vec3 ray = origin.direction().scale(distance);

        ricochetLoop: while (blockHits <= maxRicochets)
        {
            if (blockHits > 0)
                ricochets++;

            Vec3 end = rayOrigin.add(ray);

            var targets = BaseGunItem.hitScan2(shooter, originBox, rayOrigin, end, e -> true, margin);

            finalPos = targets.maxExtent().getLocation();
            for (int i = 0; i < targets.entities().size(); ++i)
            {
                var hit = targets.entities().get(i);
                entityHits.add(targets.entities().get(i));

                piercedEntities++;

                if (piercedEntities >= maxPierce)
                {
                    finalPos = hit.getLocation();
                    beamEffect.syncBeamEffect((ServerLevel) world, rayOrigin, finalPos, 0.2f, 9, 100, ricochets > 0);
                    break ricochetLoop;
                }
            }

            beamEffect.syncBeamEffect((ServerLevel) world, rayOrigin, finalPos, 0.2f, 9, 100, ricochets > 0);

            if (targets.maxExtent().getType() == HitResult.Type.MISS)
                break;

            if (ricochetChance < 1 && random.nextFloat() > ricochetChance)
                break;

            rayOrigin = targets.maxExtent().getLocation();
            blockHits++;

            // Bounce the ray
            ray = new Vec3(targets.maxExtent().getDirection().step().absolute())
                    .multiply(ray.x, ray.y, ray.z).scale(-2).add(ray);

        }

        return new Result(finalPos, entityHits, ricochets > 0);
    }

    public record Result(Vec3 finalPos, List<HitScanResult> hits, boolean ricochet)
    {

    }

    public static class HitScanResult extends EntityHitResult
    {
        private final Vec3 direction;

        public HitScanResult(Entity entity, Vec3 pos, Vec3 direction)
        {
            super(entity, pos);
            this.direction = direction;
        }

        public Vec3 getDirection()
        {
            return direction;
        }
    }

    public interface DamageHandler
    {
        void apply(Level world, LivingEntity attacker, EntityHitResult result);
    }
}
