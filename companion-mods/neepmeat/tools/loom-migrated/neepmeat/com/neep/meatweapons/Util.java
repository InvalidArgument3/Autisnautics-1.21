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

package com.neep.meatweapons;

import com.neep.meatweapons.entity.HitScan;
import com.neep.neepmeat.util.NMMaths;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Util
{
    public static Stream<HitScan.HitScanResult> getRayTargets(Entity caster, AABB box, Vec3 startPos, Vec3 endPos, Predicate<Entity> predicate, double margin)
    {
        Level world = caster.level();
        Vec3 direction = endPos.subtract(startPos).normalize();

        box = box.expandTowards(endPos.subtract(startPos)).inflate(1.0, 1.0, 1.0);
//        Box box = caster.getBoundingBox().stretch(endPos.subtract(startPos)).expand(1.0, 1.0, 1.0);

        return world.getEntities(caster, box, predicate)
                .stream()
                .mapMulti((entity, consumer) ->
        {
            @Nullable Vec3 vec3d = NMMaths.raycast(entity.getBoundingBox().inflate(entity.getPickRadius() + margin), startPos, endPos);
            if (vec3d != null)
                consumer.accept(new HitScan.HitScanResult(entity, vec3d, direction));
        });
    }

    public static Vec3 getRotationVector(float pitch, float yaw)
    {
        float h = Mth.cos(-yaw);
        float i = Mth.sin(-yaw);
        float j = Mth.cos(pitch);
        float k = Mth.sin(pitch);
        return new Vec3(i * j, -k, h * j);
    }

    @Nullable
    public static <T extends HitResult> T closestHit(Stream<T> hits, Vec3 start)
    {
        double minSquaredDistance = Double.MAX_VALUE;
        T closestHit = null;
        for (Iterator<T> it = hits.iterator(); it.hasNext(); )
        {
            T result = it.next();
            double squaredDist = result.getLocation().distanceToSqr(start);
            if (squaredDist < minSquaredDistance * minSquaredDistance)
            {
                minSquaredDistance = squaredDist;
                closestHit = result;
            }
        }

        return closestHit;
    }
}
