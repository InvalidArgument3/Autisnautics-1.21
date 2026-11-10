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
import com.neep.meatweapons.init.MWSounds;
import com.neep.neepmeat.init.NMSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BulletEntity extends MeatgunProjectileEntity
{
    @Nullable private SoundEvent ricochetSound = NMSounds.BULLET_RICOCHET.get();

    public BulletEntity(EntityType<? extends MeatgunProjectileEntity> entityType, Level world)
    {
        super(entityType, world);
    }

    public BulletEntity(Level world, double x, double y, double z, double vx, double vy, double vz)
    {
        super(MeatWeapons.BULLET, world);
        setPos(x, y, z);
        this.setDeltaMovement(vx, vy, vz);
        this.setNoGravity(true);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(net.minecraft.server.level.ServerEntity entity)
    {
//        return ProjectileSpawnPacket.create(this, MWNetwork.SPAWN_ID);
        return super.getAddEntityPacket(entity);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player)
    {
        super.stopSeenByPlayer(player);
    }

    @Override
    public void tick()
    {
        super.tick();
        if (this.level().isClientSide)
        {
//               this.spawnParticles(2);
        }
        else if (this.inGround && inGroundTime > 2 || this.moveDist > 30 || this.tickCount > 100)
        {
            this.level().broadcastEntityEvent(this, (byte)0);
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult)
    {
        spawnParticles(2);
        super.onHitBlock(blockHitResult);

        if (collisions > maxCollisions)
            setOnGround(blockHitResult);
    }

    @Override
    protected void onRicochet()
    {
        super.onRicochet();
        if (ricochetSound != null)
            playSound(NMSounds.BULLET_RICOCHET.get(), (float) random.triangle(1, 0.4),
                    (float) random.triangle(0.9, 0.3));
    }

    @Override
    protected void onEntityHitDamageFailed(Entity hitEntity, int fireTicks)
    {
        this.discard();
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent()
    {
//        return SoundEvents.BLOCK_STONE_HIT;
        return SoundEvents.EMPTY;
    }

    private void spawnParticles(int amount)
    {
        this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
    }

    protected ItemStack getPickupItem()
    {
        return ItemStack.EMPTY;
    }

    public void setRicochetSound(@Nullable SoundEvent sound)
    {
        this.ricochetSound = sound;
    }
}
