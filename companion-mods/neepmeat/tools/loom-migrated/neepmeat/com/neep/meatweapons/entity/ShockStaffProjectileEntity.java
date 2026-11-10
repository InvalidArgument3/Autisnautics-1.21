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
import com.neep.meatweapons.particle.MWParticles;
import com.neep.neepmeat.init.NMSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ShockStaffProjectileEntity extends MeatgunProjectileEntity
{
    public ShockStaffProjectileEntity(Level world)
    {
        this(MeatWeapons.SHOCK_STAFF_PROJECTILE, world);
        setNoGravity(true);
    }

    public ShockStaffProjectileEntity(EntityType<? extends MeatgunProjectileEntity> entityType, Level world)
    {
        super(entityType, world);
        setNoGravity(true);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (collisions == 0 && random.nextFloat() < 0.7)
            level().addParticle(
                    MWParticles.SHOCK_STAFF, getX(), getY(), getZ(),
                    random.triangle(0, 0.3),
                    random.triangle(0, 0.3),
                    random.triangle(0, 0.3));

        if (tickCount > 500 || collidedTicks > 2 || distanceMoved > 60)
            discard();
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

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult)
    {
        super.onHitEntity(entityHitResult);
        collideAndDiscard();
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult)
    {
        super.onHitBlock(blockHitResult);
        playSound(NMSounds.ZAP_HIT, 1, 1);
        collideAndDiscard();
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent()
    {
        return NMSounds.SHOCK_STAFF_HIT;
    }

    protected void collideAndDiscard()
    {
        if (level() instanceof ServerLevel serverWorld)
        {
            serverWorld.sendParticles(MWParticles.SHOCK_STAFF, getX(), getY(),getZ(), 10, 0.1, 0.1, 0.1, 0.1);
        }
    }

    @Override
    public void remove(RemovalReason reason)
    {
        super.remove(reason);
    }
}
