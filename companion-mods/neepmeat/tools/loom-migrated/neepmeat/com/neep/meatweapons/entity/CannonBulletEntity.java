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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class CannonBulletEntity extends AbstractArrow
{

    public CannonBulletEntity(EntityType<? extends AbstractArrow> entityType, Level world)
    {
        super(entityType, world);
    }

    public CannonBulletEntity(Level world, double x, double y, double z, double vx, double vy, double vz)
    {
        super(MeatWeapons.CANNON_BULLET, x, y, z, world);
        this.setDeltaMovement(vx, vy, vz);
        this.setNoGravity(false);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket()
    {
        return super.getAddEntityPacket();
    }

    @Override
    public void tick()
    {
        super.tick();
        if (level().isClientSide)
        {
               this.spawnParticles(2);
        }
        else if (this.inGround || this.moveDist > 30 || this.getDeltaMovement().length() < 1)
        {
            level().broadcastEntityEvent(this, (byte)0);
            this.remove(RemovalReason.DISCARDED);
        }
    }

    private void spawnParticles(int amount)
    {
        level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
    }

    protected void doPostHurtEffects(LivingEntity target)
    {
        super.doPostHurtEffects(target);
    }

    protected ItemStack getPickupItem()
    {
            return new ItemStack(Items.ARROW);
    }
}
