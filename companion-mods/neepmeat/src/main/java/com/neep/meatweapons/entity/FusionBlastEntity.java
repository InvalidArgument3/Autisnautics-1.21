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

import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.item.FusionCannonItem;
import com.neep.meatweapons.particle.MWParticles;
import com.neep.neepmeat.init.NMSounds;
import java.util.List;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class FusionBlastEntity extends AbstractArrow
{

    private static final EntityDataAccessor<Float> POWER = SynchedEntityData.defineId(FusionBlastEntity.class, EntityDataSerializers.FLOAT);

    protected float power;
    public static float MAX_POWER = FusionCannonItem.MAX_CHARGE_TIME / 2f;

    public FusionBlastEntity(EntityType<? extends AbstractArrow> entityType, Level world)
    {
        super(entityType, world);
        this.setNoGravity(false);
    }

    public FusionBlastEntity(Level world, double x, double y, double z, double vx, double vy, double vz, float power)
    {
        this(MeatWeapons.FUSION_BLAST, world);
        this.setPos(x, y, z);
        this.setDeltaMovement(vx, vy, vz);
        setBaseDamage(power / MAX_POWER * 10);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        super.defineSynchedData(builder);
        this.builder.define(POWER, power);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(net.minecraft.server.level.ServerEntity entity)
    {
        return super.getAddEntityPacket(entity);
    }

    @Override
    public void tick()
    {
        super.tick();
        entityData.set(POWER, power);
        if (level().isClientSide)
        {
               this.spawnParticles();
        }
        else if (this.inGround || this.moveDist > 30 || getDeltaMovement().length() < 0.1)
        {
            level().broadcastEntityEvent(this, (byte)0);
            remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    public boolean isNoGravity()
    {
        return true;
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent()
    {
        return NMSounds.ZAP_HIT.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult)
    {
//        DamageSource damageSource;
//        Entity entity2;
//        super.onEntityHit(entityHitResult);
//        Entity entity = entityHitResult.getEntity();
//        float f = (float)this.getVelocity().length();
//        int i = MathHelper.ceil(MathHelper.clamp((double)f * this.damage, 0.0, 2.147483647E9));
//        if (this.isCritical())
//        {
//            long l = this.random.nextInt(i / 2 + 2);
//            i = (int)Math.min(l + (long)i, Integer.MAX_VALUE);
//        }
//        if ((entity2 = this.getOwner()) == null) {
//            damageSource = DamageSource.arrow(this, this);
//        } else {
//            damageSource = DamageSource.arrow(this, entity2);
//            if (entity2 instanceof LivingEntity) {
//                ((LivingEntity)entity2).onAttacking(entity);
//            }
//        }
//        boolean bl = entity.getType() == EntityType.ENDERMAN;
//        int j = entity.getFireTicks();
//        if (this.isOnFire() && !bl) {
//            entity.setOnFireFor(5);
//        }
//        if (entity.damage(damageSource, i)) {
//            if (bl) {
//                return;
//            }
//            if (entity instanceof LivingEntity) {
//                Vec3d vec3d;
//                LivingEntity livingEntity = (LivingEntity)entity;
//                if (!this.world.isClient && this.getPierceLevel() <= 0) {
//                    livingEntity.setStuckArrowCount(livingEntity.getStuckArrowCount() + 1);
//                }
//                if (this.punch > 0 && (vec3d = this.getVelocity().multiply(1.0, 0.0, 1.0).normalize().multiply((double)this.punch * 0.6)).lengthSquared() > 0.0) {
//                    livingEntity.addVelocity(vec3d.x, 0.1, vec3d.z);
//                }
//                if (!this.world.isClient && entity2 instanceof LivingEntity) {
//                    EnchantmentHelper.onUserDamaged(livingEntity, entity2);
//                    EnchantmentHelper.onTargetDamaged((LivingEntity)entity2, livingEntity);
//                }
//                this.onHit(livingEntity);
//                if (entity2 != null && livingEntity != entity2 && livingEntity instanceof PlayerEntity && entity2 instanceof ServerPlayerEntity && !this.isSilent()) {
//                    ((ServerPlayerEntity)entity2).networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.PROJECTILE_HIT_PLAYER, GameStateChangeS2CPacket.DEMO_OPEN_SCREEN));
//                }
//                if (!entity.isAlive() && this.piercingKilledEntities != null) {
//                    this.piercingKilledEntities.add(livingEntity);
//                }
//                if (!this.world.isClient && entity2 instanceof ServerPlayerEntity) {
//                    ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity2;
//                    if (this.piercingKilledEntities != null && this.isShotFromCrossbow()) {
//                        Criteria.KILLED_BY_CROSSBOW.trigger(serverPlayerEntity, this.piercingKilledEntities);
//                    } else if (!entity.isAlive() && this.isShotFromCrossbow()) {
//                        Criteria.KILLED_BY_CROSSBOW.trigger(serverPlayerEntity, Arrays.asList(entity));
//                    }
//                }
//            }
//            this.playSound(this.sound, 1.0f, 1.2f / (this.random.nextFloat() * 0.2f + 0.9f));
//            if (this.getPierceLevel() <= 0) {
//                this.discard();
//            }
//        } else {
//            entity.setFireTicks(j);
//            this.setVelocity(this.getVelocity().multiply(-0.1));
//            this.setYaw(this.getYaw() + 180.0f);
//            this.prevYaw += 180.0f;
//            if (!this.world.isClient && this.getVelocity().lengthSquared() < 1.0E-7) {
//                if (this.pickupType == PickupPermission.ALLOWED) {
//                    this.dropStack(this.asItemStack(), 0.1f);
//                }
//                this.discard();
//            }
//        }
        explode();
        spawnHitParticles();
        if (!level().isClientSide)
        {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult)
    {
        super.onHitBlock(blockHitResult);
        explode();
        spawnHitParticles();
    }

    protected void explode()
    {
        float r = 2.5f;
        AABB box = getBoundingBox().inflate(r);
        List<Entity> entities = level().getEntities(null, box);
        entities.forEach(e ->
        {
            // Damage reduces linearly with distance
//            double d = getDamage() * (centre.distanceTo(e.getPos()) / r);
//            e.damage(world.getDamageSources().mobProjectile(this, getOwner() instanceof LivingEntity le ? le : null), (float) d);
            e.hurt(level().damageSources().mobProjectile(this, getOwner() instanceof LivingEntity le ? le : null), (float) getBaseDamage());
        });
    }

    protected void spawnHitParticles()
    {
        if (level() instanceof ServerLevel serverWorld)
        {
            for (int i = 0; i < 10; ++i)
            {
//                this.world.addParticle(MWParticles.PLASMA_PARTICLE, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
                serverWorld.sendParticles(MWParticles.PLASMA_PARTICLE, getX(), getY(), getZ(), 15, 0.1, 0.1, 0.1, 0.1);
            }
        }
    }

    protected void spawnParticles()
    {
        level().addParticle(MWParticles.PLASMA_PARTICLE,
                this.getX() + (random.nextFloat() - 0.5) / 2,
                this.getY() + (random.nextFloat() - 0.5) / 2,
                this.getZ() + (random.nextFloat() - 0.5) / 2,
                0, 0, 0);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target)
    {
        super.doPostHurtEffects(target);
    }

    @Override
    protected ItemStack getPickupItem()
    {
        return ItemStack.EMPTY;
    }

    public float getPower()
    {
        return entityData.get(POWER);
    }
}
