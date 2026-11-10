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

import net.minecraft.network.syncher.SynchedEntityData;

import net.minecraft.core.HolderLookup;

import com.neep.neepmeat.entity.follower.FollowerEntity;
import com.neep.neepmeat.init.NMEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class LimbEntity extends SimpleEntity
{
    private final int maxAge;
    public boolean squirm = false;

    public LimbEntity(EntityType<? extends LimbEntity> entityType, Level world)
    {
        super(entityType, world);
        maxAge = 300;
    }

    public LimbEntity(Level world, int maxAge, boolean squirm)
    {
        super(NMEntities.LIMB, world);
        this.maxAge = maxAge;
        this.squirm = squirm;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        super.defineSynchedData(builder);
    }

    @Override
    public void tick()
    {
        super.tick();

        tickMovement();

        if (squirm)
        {
            setYRot((float) (Math.random() * 360));
            squirm = false;
        }

        if (tickCount > maxAge)
        {
            remove(RemovalReason.DISCARDED);
        }
    }

    public boolean shouldSwim()
    {
        return true;
    }

    @Override
    public CompoundTag saveWithoutId(CompoundTag nbt)
    {
        return super.saveWithoutId(nbt);
    }

    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt)
    {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {

    }

    @Override
    public boolean canCollideWith(Entity other)
    {
        return !(other instanceof FollowerEntity);
    }

    @Override
    public void push(Entity entity)
    {
        // I can't work out how to make the Follower not collide with any entity.
        if (entity instanceof FollowerEntity)
            return;

        super.push(entity);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(net.minecraft.server.level.ServerEntity entity)
    {
        return new ClientboundAddEntityPacket(this);
    }
}
