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

package com.neep.meatweapons.entity;

import com.neep.meatweapons.EntityCritRegistry;
import com.neep.meatweapons.Util;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.meatgun.module.BoneStealerModule;
import com.neep.meatweapons.meatgun.module.MeatgunModules;
import com.neep.neepmeat.init.NMParticles;
import com.neep.neepmeat.init.NMSounds;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class MeatgunProjectileEntity extends AbstractArrow
{
    private static final EntityDataAccessor<Integer> COL = SynchedEntityData.defineId(MeatgunProjectileEntity.class, EntityDataSerializers.INT);

    protected float homingRadius;
    protected float homingSpeed;
    @Nullable
    protected Entity homingTarget;
    protected boolean prioritiseHoming;

    protected int collisions = 0;
    protected int collidedTicks = 0;
    protected float distanceMoved = 0;
    protected float meatgunPunch = 0;
    protected int maxCollisions = 0;
    protected float bounceMultiplier = 1.5f;
    private double maxDistance = 80;
    private boolean boneStealing;

    public MeatgunProjectileEntity(EntityType<? extends MeatgunProjectileEntity> entityType, Level world)
    {
        super(entityType, world);
    }

    public static void setupProjectileAttributes(MeatgunProjectileEntity entity, RootModuleHolder holder, float homingRadius)
    {
        if (holder.containsType(MeatgunModules.HOMING_BRAIN))
            entity.setHomingRadius(homingRadius);

        entity.setBoneStealing(holder.containsType(MeatgunModules.BONE_STEALER));
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        entityData.define(COL, 0x80000000); // Grey by default
    }

    @Override
    public void tick()
    {
        double prevX = getX();
        double prevY = getY();
        double prevZ = getZ();
        super.tick();
        double dx = getX() - prevX;
        double dy = getY() - prevY;
        double dz = getZ() - prevZ;
        distanceMoved += (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        tickHoming();

        if (collisions > 0)
            collidedTicks++;

        if (distanceMoved >= maxDistance)
            discard();
    }

    protected void tickHoming()
    {
        if (level().isClientSide())
            return;

        if (homingRadius > 0 && homingTarget == null)
        {
            if (tickCount % 2 == 0)
            {
                var vel = getDeltaMovement();
                AABB box = AABB.ofSize(position(), homingRadius * 2, homingRadius * 2, homingRadius * 2)
                        .inflate(vel.x, vel.y, vel.z);
                List<LivingEntity> entityList = new ArrayList<>();
                level().getEntities(
                        EntityTypeTest.forClass(LivingEntity.class), box, e ->
                                e != getOwner() && canHitEntity(e),
                        entityList, 7);

                double leastDistance = Double.MAX_VALUE;
                Entity target = null;
                for (var entity : entityList)
                {
                    double distance = entity.distanceToSqr(this);
                    if (distance < leastDistance)
                    {
                        leastDistance = distance;
                        target = entity;
                    }
                }

                if (target != null)
                {
                    this.homingTarget = target;
                }
            }
        }

        if (homingTarget != null && canHitEntity(homingTarget))
        {
            Vec3 vel = getDeltaMovement();
            if (prioritiseHoming)
                vel = vel.scale(0.8f);

            Vec3 toTarget = new Vec3(
                    homingTarget.getX() - getX(),
                    homingTarget.getEyeY() - getY(),
                    homingTarget.getZ() - getZ()
            ).normalize()
                    .scale(homingSpeed >= 0 ? homingSpeed : flyDist);

            setDeltaMovement(vel.add(toTarget));
        }
        else
        {
            homingTarget = null;
        }
    }

    @Override
    protected boolean tryPickup(Player player)
    {
        return false;
    }

    protected boolean canHitEntity(Entity entity)
    {
        return super.canHitEntity(entity) && !ownedBy(entity);
    }

    @Override
    public void shootFromRotation(Entity shooter, float pitch, float yaw, float roll, float speed, float divergence)
    {
        super.shootFromRotation(shooter, pitch, yaw, roll, speed, divergence);

        if (homingSpeed <= 0)
            setHomingSpeed(speed / 2, prioritiseHoming);
    }

    @Override
    protected ItemStack getPickupItem()
    {
        return ItemStack.EMPTY;
    }

    public void setBulletPunch(float punch)
    {
        this.meatgunPunch = punch;
    }

    public void setMaxDistance(double maxDistance)
    {
        this.maxDistance = maxDistance;
    }

    protected void onHit(HitResult hitResult)
    {
        HitResult.Type type = hitResult.getType();
        if (type == HitResult.Type.ENTITY)
        {
            this.onHitEntity((EntityHitResult) hitResult);
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, hitResult.getLocation(), GameEvent.Context.of(this, null));
        }
        else if (type == HitResult.Type.BLOCK)
        {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            this.onHitBlock(blockHitResult);
            BlockPos blockPos = blockHitResult.getBlockPos();
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, blockPos, GameEvent.Context.of(this, this.level().getBlockState(blockPos)));
        }
    }

    @Nullable
    protected EntityHitResult findHitEntity(Vec3 currentPosition, Vec3 nextPosition)
    {
        Stream<HitScan.HitScanResult> hits = Util.getRayTargets(this, getBoundingBox().expandTowards(getDeltaMovement()).inflate(1.0),
                currentPosition,
                nextPosition,
                this::canHitEntity,
                0.3f);

        return Util.closestHit(hits, currentPosition);

        // Vanilla ProjectileUtil does not account for the actual hit pos, it just gives the entity's base position.
//        var coll = ProjectileUtil.getEntityCollision(
//                this.getWorld(), this, currentPosition, nextPosition, this.getBoundingBox().stretch(this.getVelocity()).expand(1.0), this::canHit
//        );
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult)
    {
        collisions++;

        // Recreation of vanilla mechanics with some differences, namely the removal of spee as a damage modifier.
        Entity hitEntity = entityHitResult.getEntity();

        float damage = (float) getBaseDamage(); // This is where speed would be applied in vanilla

        // Vanilla crits are perplexing
//        if (this.isCritical())
//        {
//            long l = this.random.nextInt(damage / 2 + 2);
//            damage = (int) Math.min(l + (long) damage, 2147483647L);
//        }

        float f = modifyDamage(hitEntity.level(), damage, entityHitResult, getDeltaMovement().normalize());
        boolean crit = f > 1;

        damage *= f;

        // Fractional damage seems to cause a desync that is most evident when the mob dies (or appears to die).
        // However, I can't get rid of fractional damage as the precision is needed for higher ROF guns.
        // The easy solution is to only handle damage on the server.
        if (level().isClientSide())
            return;

        Entity owner = this.getOwner();
        DamageSource damageSource = createDamageSource(owner, hitEntity, crit);

        boolean enderman = hitEntity.getType() == EntityType.ENDERMAN;
        int j = hitEntity.getRemainingFireTicks();
        if (this.isOnFire() && !enderman)
        {
            hitEntity.setSecondsOnFire(5);
        }

        if (hitEntity.hurt(damageSource, damage))
        {
            if (enderman)
            {
                return;
            }

            if (hitEntity instanceof LivingEntity livingEntity)
            {
                if (meatgunPunch > 0)
                {
                    double d = Math.max(0.0, 1.0 - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                    Vec3 vec3d = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale((double) meatgunPunch * 0.6 * d);
                    if (vec3d.lengthSqr() > 0.0)
                    {
                        livingEntity.push(vec3d.x, 0.1, vec3d.z);
                    }
                }

                if (owner instanceof LivingEntity livingOwner)
                {
                    if (boneStealing)
                        BoneStealerModule.apply(livingOwner, hitEntity, damage);
                }

                if (!this.level().isClientSide && owner instanceof LivingEntity)
                {
                    EnchantmentHelper.doPostHurtEffects(livingEntity, owner);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) owner, livingEntity);
                }

                this.doPostHurtEffects(livingEntity);
            }

            this.playSound(getHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            if (this.getPierceLevel() <= 0)
            {
                this.discard();
            }
        }
        else
        {
            onEntityHitDamageFailed(hitEntity, j);
        }
    }

    public static float modifyDamage(Level world, float damage, EntityHitResult ehr, Vec3 direction)
    {
        float f = critMultiplier(ehr, direction);
        if (f > 1)
        {
            if (world instanceof ServerLevel serverWorld)
            {
                var pos = ehr.getLocation();
                float d = 0.3f;
                serverWorld.sendParticles(NMParticles.BLOOD_SHOWER,
                        pos.x, pos.y, pos.z,
                        10,
                        d, d, d, 0.1);

//                serverWorld.playSound(null, pos.x, pos.y, pos.z, MWSounds.BULLET_HIT_LIVING_CRIT, SoundCategory.PLAYERS,
//                        1, 1);
            }
        }
        return f;
    }

    public static float critMultiplier(EntityHitResult ehr, Vec3 direction)
    {
        return EntityCritRegistry.get(ehr.getEntity()).apply(ehr.getEntity(), ehr.getLocation(), direction);
    }

    protected DamageSource createDamageSource(Entity owner, Entity hitEntity, boolean critical)
    {
        if (owner instanceof LivingEntity livingEntity)
        {
            livingEntity.setLastHurtMob(hitEntity);
            return BulletDamageSource.create(level(), livingEntity, meatgunPunch, critical);
        }
        else
        {
            return BulletDamageSource.create(level(), null, meatgunPunch, critical);
        }
    }

    protected void onEntityHitDamageFailed(Entity hitEntity, int fireTicks)
    {
        // Bounce
        hitEntity.setRemainingFireTicks(fireTicks);

        this.setDeltaMovement(this.getDeltaMovement().scale(-0.1));
        this.setYRot(this.getYRot() + 180.0F);
        this.yRotO += 180.0F;
        if (!this.level().isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7)
        {
            if (this.pickup == AbstractArrow.Pickup.ALLOWED)
            {
                this.spawnAtLocation(this.getPickupItem(), 0.1F);
            }

            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult)
    {
        if (collisions < maxCollisions)
        {
            Vec3 vel = getDeltaMovement();
            Vector3d normal = new Vector3d(blockHitResult.getDirection().step().absolute())
                    .mul(vel.x, vel.y, vel.z) // Select the component to invert
                    .mul(-bounceMultiplier);

            setDeltaMovement(vel.x + normal.x, vel.y + normal.y, vel.z + normal.z);
            onRicochet();
        }
        else
        {
            setOnGround(blockHitResult);
        }

        collisions++;
    }

    protected void onRicochet()
    {

    }

    protected void setOnGround(BlockHitResult result)
    {
        Vec3 vec3d = result.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(vec3d);
        Vec3 vec3d2 = vec3d.normalize().scale(0.05F);
        this.setPosRaw(this.getX() - vec3d2.x, this.getY() - vec3d2.y, this.getZ() - vec3d2.z);

        this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
        this.inGround = true;

        this.setCritArrow(false);
        this.setPierceLevel((byte)0);
    }

    @Override
    public void playSound(@Nullable SoundEvent sound, float volume, float pitch)
    {
        if (sound == null)
            return;

        super.playSound(sound, volume, pitch);
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent()
    {
        return NMSounds.SHOCK_STAFF_HIT;
    }

    public void setHomingRadius(float homingRadius)
    {
        this.homingRadius = homingRadius;
    }

    public void setHomingTarget(@Nullable Entity target)
    {
        this.homingTarget = target;
    }

    public void setHomingSpeed(float homingSpeed, boolean prioritiseHoming)
    {
        this.homingSpeed = homingSpeed;
        this.prioritiseHoming = prioritiseHoming;
    }

    public void setCol(int col)
    {
        entityData.set(COL, col);
    }

    public int getCol()
    {
        return entityData.get(COL);
    }

    public void setMaxCollisions(int maxCollisions, float bounceMultiplier)
    {
        this.maxCollisions = maxCollisions;
        this.bounceMultiplier = bounceMultiplier;
    }

    public MeatgunProjectileEntity setBoneStealing(boolean boneStealing)
    {
        this.boneStealing = boneStealing;
        return this;
    }

    @Override
    public CompoundTag saveWithoutId(CompoundTag nbt)
    {
        super.saveWithoutId(nbt);
        nbt.putFloat("homing_range", homingRadius);
        nbt.putBoolean("prioritise_homing", prioritiseHoming);
        nbt.putFloat("homing_speed", homingSpeed);
        return nbt;
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.homingRadius = nbt.getFloat("homing_range");
        this.prioritiseHoming = nbt.getBoolean("prioritise_homing");
        this.homingSpeed = nbt.getFloat("homing_speed");
    }
}
