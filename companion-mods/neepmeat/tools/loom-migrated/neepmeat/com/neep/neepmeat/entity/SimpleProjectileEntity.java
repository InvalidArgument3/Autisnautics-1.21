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

package com.neep.neepmeat.entity;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SimpleProjectileEntity extends Projectile
{
    public SimpleProjectileEntity(EntityType<? extends Projectile> entityType, Level world)
    {
        super(entityType, world);
    }

    public static Vec3 makeVelocity(double x, double y, double z, float speed, float divergence, RandomSource random)
    {
        return new Vec3(x, y, z)
                .normalize()
                .add(
                        random.triangle(0.0, 0.0172275 * (double) divergence),
                        random.triangle(0.0, 0.0172275 * (double) divergence),
                        random.triangle(0.0, 0.0172275 * (double) divergence)
                )
                .scale(speed);
    }

    public void shootWithVelocity(Vec3 vec3d)
    {
        this.setDeltaMovement(vec3d);
        double d = vec3d.horizontalDistance();
        this.setYRot((float) (Mth.atan2(vec3d.x, vec3d.z) * 180.0F / (float) Math.PI));
        this.setXRot((float) (Mth.atan2(vec3d.y, d) * 180.0F / (float) Math.PI));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    public void tick()
    {
        super.tick();

        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        boolean bl = false;
        if (hitResult.getType() != HitResult.Type.MISS && !bl)
        {
            this.onHit(hitResult);
        }

        checkInsideBlocks();
        Vec3 vec3d = this.getDeltaMovement();
//        double d = this.getX() + vec3d.x;
//        double e = this.getY() + vec3d.y;
//        double f = this.getZ() + vec3d.z;
        this.updateRotation();

//        float multiplier;
//        if (this.isTouchingWater())
//        {
//            multiplier = 0.8f;
//        }
//        else
//        {
//            multiplier = 0.99f;
//        }
//        this.setVelocity(vec3d.multiply(multiplier));

        if (!this.isNoGravity())
        {
            Vec3 vec3d2 = this.getDeltaMovement();
            this.setDeltaMovement(vec3d2.x, vec3d2.y - this.getGravity(), vec3d2.z);
        }

        move(MoverType.SELF, getDeltaMovement());
    }

    protected double getGravity()
    {
        return 0;
    }

    @Override
    protected void defineSynchedData()
    {

    }
}
