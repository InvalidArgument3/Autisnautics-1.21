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

import com.neep.meatweapons.MWItems;
import com.neep.meatweapons.MeatWeapons;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ExplodingShellEntity extends AbstractArrow
{
    public static final ExplosionDamageCalculator DESTROY_BEHAVIOUR = new ExplosionDamageCalculator();
    public static final ExplosionDamageCalculator KEEP_BEHAVIOUR = new ExplosionDamageCalculator()
    {
        @Override
        public boolean shouldBlockExplode(Explosion explosion, BlockGetter world, BlockPos pos, BlockState state, float power)
        {
            return false;
        }
    };

    protected int explosionPower = 1;
    protected boolean destructive;

    public ExplodingShellEntity(EntityType<? extends AbstractArrow> entityType, Level world)
    {
        super(entityType, world);
    }

    public ExplodingShellEntity(Level world, int explosionPower, boolean destructive, double x, double y, double z, double vx, double vy, double vz)
    {
        super(MeatWeapons.EXPLODING_SHELL, x, y, z, world);
        this.setDeltaMovement(vx, vy, vz);
        this.setNoGravity(false);
        this.explosionPower = explosionPower;
        this.destructive = destructive;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(net.minecraft.server.level.ServerEntity entity)
    {
//        return ProjectileSpawnPacket.create(this, MWNetwork.SPAWN_ID);
        return super.getAddEntityPacket(entity);
    }

    @Override
    public void shoot(double x, double y, double z, float speed, float divergence)
    {
        super.shoot(x, y, z, speed, divergence);
    }

    @Override
    public void setPos(double x, double y, double z)
    {
        super.setPos(x, y, z);
    }

    @Override
    public void tick()
    {
        super.tick();
        if (this.level().isClientSide)
        {
               this.spawnParticles(5);
        }
        else if (this.inGround || this.moveDist > 200)
        {
//            explode();
            this.level().broadcastEntityEvent(this, (byte) 0);
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    protected void onHit(HitResult hitResult)
    {
        super.onHit(hitResult);
        if (!level().isClientSide)
        {
            if (hitResult.getType() == HitResult.Type.BLOCK)
            {
                explode();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult)
    {
        super.onHitEntity(entityHitResult);
        if (!level().isClientSide)
        {
            explode();
        }
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent()
    {
        return SoundEvents.STONE_HIT;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
        nbt.putByte("ExplosionPower", (byte) this.explosionPower);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("ExplosionPower", 99))
        {
            this.explosionPower = nbt.getByte("ExplosionPower");
        }
    }

    protected void explode()
    {
        boolean mobGriefing = level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && destructive;
//        this.getWorld().createExplosion(null, this.getX(), this.getY(), this.getZ(), this.explosionPower, mobGriefing, World.ExplosionSourceType.NONE);
        this.level().explode(null, null, mobGriefing ? DESTROY_BEHAVIOUR : KEEP_BEHAVIOUR,
                this.getX(), this.getY(), this.getZ(), this.explosionPower, mobGriefing, Level.ExplosionInteraction.BLOCK);
    }

    private void spawnParticles(int amount)
    {
//        Random rand = new Random((long) getX());
//        System.out.println(distanceTraveled);
        double x = 0.05;
        for (int i = 0; i < amount; ++i)
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), x * random.nextGaussian(), x * random.nextGaussian(), x * random.nextGaussian());
    }

    protected void doPostHurtEffects(LivingEntity target)
    {
        super.doPostHurtEffects(target);
    }

    protected ItemStack getPickupItem()
    {
            return new ItemStack(MWItems.SMALL_BALLISTIC_MAGAZINE);
    }
}
