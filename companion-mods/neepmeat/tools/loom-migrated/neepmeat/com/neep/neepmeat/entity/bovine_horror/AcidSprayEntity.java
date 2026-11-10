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
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AcidSprayEntity extends Projectile
{
    public AcidSprayEntity(EntityType<? extends Projectile> entityType, Level world)
    {
        super(entityType, world);
    }

    public void target(Vec3 origin, Vec3 targetPos, float hSpeed, float divergence)
    {
        double dx = targetPos.x - origin.x;
        double dz = targetPos.z - origin.z;
        double dy = targetPos.y - origin.y;

        double distance = Math.sqrt(dx * dx + dz * dz);

        double time = distance / hSpeed;

        // s = ut + at^2/2
        double uy = (dy - (-getGravity() * time * time) / 2) / time;

        // Normalise and multiply by hSpeed
        double ux = dx / distance * hSpeed;
        double uz = dz / distance * hSpeed;

        setPos(origin.x(), origin.y(), origin.z());
        setDeltaMovement(ux, uy, uz);
        this.setYRot((float) (Mth.atan2(ux, uz) * 57.2957763671875));
        this.setXRot((float) (Mth.atan2(uy, hSpeed) * 57.2957763671875));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    protected void defineSynchedData()
    {

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
        double d = this.getX() + vec3d.x;
        double e = this.getY() + vec3d.y;
        double f = this.getZ() + vec3d.z;
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
        this.setPos(d, e, f);

        if (level().isClientSide())
        {
           int count = 10;

            for (int i = 0; i < count; ++i)
            {
                double px = position().x + (Math.random() - 0.5) * 2;
                double py = getEyeY() + (Math.random() - 0.5) * 2;
                double pz = position().z + (Math.random() - 0.5) * 2;
                Vec3 vel = getDeltaMovement();
                level().addParticle(NMParticles.BODY_COMPOUND_SHOWER, px, py, pz, vel.x, vel.y, vel.z);
            }
        }
    }

    @Override
    public boolean isNoGravity()
    {
        return false;
    }

    protected void onHit(HitResult hitResult)
    {
        HitResult.Type type = hitResult.getType();
//        if (type == HitResult.Type.ENTITY)
//        {
//            this.onEntityHit((EntityHitResult)hitResult);
//            this.getWorld().emitGameEvent(GameEvent.PROJECTILE_LAND, hitResult.getPos(), GameEvent.Emitter.of(this, null));
//        }
        if (type == HitResult.Type.BLOCK)
        {
            BlockHitResult blockHitResult = (BlockHitResult)hitResult;
            this.onHitBlock(blockHitResult);
            BlockPos blockPos = blockHitResult.getBlockPos();
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, blockPos, GameEvent.Context.of(this, this.level().getBlockState(blockPos)));
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult)
    {
        super.onHitBlock(blockHitResult);
       onHit();
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult)
    {
        super.onHitEntity(entityHitResult);
        onHit();
    }

    protected float getGravity()
    {
        return 0.03f;
    }

    protected void onHit()
    {
        level().getEntities(this, getBoundingBox().inflate(2), e -> e instanceof LivingEntity
                && !e.getType().equals(NMEntities.BOVINE_HORROR)).forEach(e ->
        {
            float dist = e.distanceTo(this);
            float damage = 8;
            if (dist > 1.5)
                damage = 5;
            else if (dist > 2.5)
                damage = 3;

            e.hurt(level().damageSources().mobAttack((LivingEntity) getOwner()), damage);
        });

        if (level().isClientSide())
        {
            var effect = NMParticles.BODY_COMPOUND_SHOWER;
            for (int i = 0; i < 100; ++i)
            {
                double r = random.nextFloat() * 2;
                double yaw = (random.nextFloat() + 0.5) * 2 * Math.PI;
                double px = getX() + r * Math.sin(yaw);
                double pz = getZ() + r * Math.cos(yaw);
                double py = getY();

                double vx = (px - getX()) * 0.2;
                double vy = r * r * 0.1;
                double vz = (pz - getZ()) * 0.2;

               level().addParticle(effect, px, py, pz, vx, vy, vz);
            }
        }

        discard();
    }
}
