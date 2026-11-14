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
import com.neep.meatweapons.damage.MWDamageSources;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class BounceGrenadeEntity extends MeatgunProjectileEntity
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

    protected float explosionPower = 1;
    protected boolean destructive;
    protected int fuse = 100;

    public BounceGrenadeEntity(EntityType<BounceGrenadeEntity> entityType, Level world)
    {
        super(entityType, world);
    }

    public BounceGrenadeEntity(Level world, float explosionPower, int fuse, boolean destructive)
    {
        super(MeatWeapons.BOUNCE_GRENADE, world);
        this.setNoGravity(false);
        this.fuse = fuse;
        this.explosionPower = explosionPower;
        this.destructive = destructive;
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
        if (this.level().isClientSide)
        {
            this.spawnParticles(1);
        }
        else if (this.tickCount > fuse)
        {
            explode();
        }
    }

    @Override
    protected void onHit(HitResult hitResult)
    {
//        HitResult.Type type = hitResult.getType();
//        if (type == HitResult.Type.ENTITY)
//        {
//            this.onEntityHit((EntityHitResult) hitResult);
//            this.getWorld().emitGameEvent(GameEvent.PROJECTILE_LAND, hitResult.getPos(), GameEvent.Emitter.of(this, (BlockState) null));
//        }
//        else if (type == HitResult.Type.BLOCK)
//        {
//            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
//            this.onBlockHit(blockHitResult);
//            BlockPos blockPos = blockHitResult.getBlockPos();
//
//            Vec3d vel = getVelocity();
//            Vector3d normal = new Vector3d(blockHitResult.getSide().getUnitVector())
//                    .mul(vel.x, vel.y, vel.z)
//                    .mul(-2);
//
//            setVelocity(vel.x + normal.x, vel.y + normal.y, vel.z + normal.z);
//        }
//
//        if (!getWorld().isClient)
//        {
//            if (hitResult.getType() == HitResult.Type.BLOCK)
//            {
//            }
//        }
        super.onHit(hitResult);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance)
    {
        return distance <= 200;
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
    protected void onInsideBlock(BlockState state)
    {
        super.onInsideBlock(state);
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
        nbt.putFloat("ExplosionPower", this.explosionPower);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        this.explosionPower = nbt.getFloat("ExplosionPower");
    }

    protected void explode()
    {
        Vec3 explosionPos = position();
        if (inGround)
        {

        }

        boolean mobGriefing = level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && destructive;

        this.level().explode(null,
                damageSources().source(MWDamageSources.MEATGUN_EXPLOSION, getOwner()),
                KEEP_BEHAVIOUR,
                explosionPos.x, explosionPos.y, explosionPos.z, this.explosionPower, mobGriefing, Level.ExplosionInteraction.BLOCK);

        remove(RemovalReason.DISCARDED);
        this.level().broadcastEntityEvent(this, (byte) 0);
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

    public boolean isInGround()
    {
        return inGround;
    }
}
