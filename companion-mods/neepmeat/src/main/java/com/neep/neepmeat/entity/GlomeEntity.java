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

import com.neep.neepmeat.init.NMEntities;
import com.neep.neepmeat.init.NMParticles;
import com.neep.neepmeat.init.NMSounds;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GlomeEntity extends FlyingMob
{
    protected int time;
    protected final int initialRandom;

    protected static final EntityDataAccessor<Integer> MAX_TIME = SynchedEntityData.defineId(GlomeEntity.class, EntityDataSerializers.INT);

    public GlomeEntity(EntityType<? extends FlyingMob> entityType, Level world)
    {
        super(entityType, world);
        this.setNoGravity(true);
        this.setCustomNameVisible(false);
        this.initialRandom = getRandom().nextInt(180);
    }

    public GlomeEntity(Level world, double x, double y, double z, double vx, double vy, double vz)
    {
        this(NMEntities.GLOME, world);
        this.setPosRaw(x, y, z);
        this.setDeltaMovement(vx, vy, vz);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        super.defineSynchedData();
        entityData.define(MAX_TIME, 160);
    }

    @Override
    public Iterable<ItemStack> getArmorSlots()
    {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack)
    {

    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket()
    {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet)
    {
        super.recreateFromPacket(packet);
        spawnParticles(20, 0.5f);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (this.tickCount >= getMaxTime())
        {
            this.remove(RemovalReason.DISCARDED);
            spawnVanishParticles(10, 1);
        }
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source)
    {
        return NMSounds.GLOME_HIT.get();
    }

    @Override
    public HumanoidArm getMainArm()
    {
        return HumanoidArm.LEFT;
    }

    public int getTimeInWorld()
    {
        return time;
    }

    private void spawnParticles(int amount, float d)
    {
        for (int i = 0; i < amount; ++i)
        {
            level().addParticle(NMParticles.MEAT_BIT, getX(), getY(), getZ(),
                    d * (random.nextFloat() - 0.5),
                    d * (random.nextFloat() - 0.5),
                    d * (random.nextFloat() - 0.5));
        }
    }

    private void spawnVanishParticles(int amount, float d)
    {
        for (int i = 0; i < amount; ++i)
        {
            level().addParticle(NMParticles.MEAT_BIT, getX(), getY() + getBbWidth() / 2, getZ(),
                    d * (random.nextFloat() - 0.5),
                    d * (random.nextFloat() - 0.5),
                    d * (random.nextFloat() - 0.5));
        }
    }

    public int getMaxTime()
    {
        return 120;
    }

    public int getInitialRandom()
    {
        return initialRandom;
    }
}