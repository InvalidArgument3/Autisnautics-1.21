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

import net.minecraft.core.HolderLookup;

import com.neep.neepmeat.init.NMEntities;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.init.NMItems;
import com.neep.neepmeat.init.NMParticles;
import com.neep.neepmeat.item.EssentialSaltesItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;

public class MobEggEntity extends SimpleEntity
{
    private static final EntityDataAccessor<Integer> WOBBLE_TICKS = SynchedEntityData.defineId(MobEggEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> WOBBLE_STRENGTH = SynchedEntityData.defineId(MobEggEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WOBBLE_DIRECTION = SynchedEntityData.defineId(MobEggEntity.class, EntityDataSerializers.FLOAT);

    protected EntityType<?> hatchType;
    protected int growthTicks;

    public MobEggEntity(EntityType<? extends Entity> entityType, Level world)
    {
        super(entityType, world);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        super.defineSynchedData(builder);
        this.builder.define(WOBBLE_TICKS, 0);
        this.builder.define(WOBBLE_STRENGTH, 0f);
        this.builder.define(WOBBLE_DIRECTION, 1f);
    }

    public MobEggEntity(Level world, EntityType<?> hatchType)
    {
        super(NMEntities.EGG, world);
        this.hatchType = hatchType;
    }

    @Override
    public void tick()
    {
        super.tick();

        this.setWobbleTicks(Math.max(0, this.getWobbleTicks() - 1));
        this.setWobbleStrength(Math.max(0, this.getWobbleStrength() - 1));
        tickMovement();
        if (random.nextFloat() < 0.005)
        {
            setWobbleTicks(10);
            setWobbleStrength(10);
            invertWobbleDirection();
        }

        if (canGrow())
        {
            ++growthTicks;
        }

        if (growthTicks > 100)
        {
            hatch();
        }
    }

    @Override
    public boolean canCollideWith(Entity other)
    {
        return super.canCollideWith(other) && !(other instanceof Minecart);
    }

    @Override
    public void playerTouch(Player player)
    {
        super.playerTouch(player);
    }

    public boolean shouldSwim()
    {
        return true;
    }

    public void hatch()
    {
        if (!level().isClientSide() && getHatchType() != null)
        {
            Entity vehicle = getVehicle();
            this.remove(RemovalReason.DISCARDED);

            Entity entity = getHatchType().create(level());
            if (entity != null)
            {
                entity.setPos(position());
                level().addFreshEntity(entity);

                if (vehicle != null)
                {
                    removeVehicle();
                    entity.startRiding(vehicle);
                }
            }

            ((ServerLevel) level()).sendParticles(NMParticles.MEAT_SPLASH, entity.getX(), entity.getY(), entity.getZ(), 30, 1, 2, 1, 0.01);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount)
    {
        setWobbleTicks(10);
        setWobbleStrength(getWobbleStrength() + amount * 10.0f);
        invertWobbleDirection();
        this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
        if (!level().isClientSide() && getWobbleStrength() > 30.0f)
        {
            this.onDeath();
        }
        return super.hurt(source, amount);
    }

    public void onDeath()
    {
        if (this.isRemoved())
        {
            return;
        }
        ItemStack stack = new ItemStack(NMItems.MOB_EGG);
        EssentialSaltesItem.putEntityType(stack, hatchType);
        spawnAtLocation(stack);
        this.kill();
        this.level().broadcastEntityEvent(this, (byte)3);
    }

    public boolean canGrow()
    {
        FluidState state = level().getFluidState(blockPosition());
        return state.getType() == com.neep.neepmeat.fluid.FluidForms.still(NMFluids.BLOOD) || state.getType() == com.neep.neepmeat.fluid.FluidForms.flowing(NMFluids.BLOOD);
    }

    @Override
    public CompoundTag saveWithoutId(CompoundTag nbt)
    {
        super.saveWithoutId(nbt);
        if (hatchType != null) nbt.putString("hatchType", BuiltInRegistries.ENTITY_TYPE.getKey(hatchType).toString());
        return nbt;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        this.hatchType = nbt.contains("hatchType") ? BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(nbt.getString("hatchType"))) : null;
    }

    public void setWobbleTicks(int wobbleTicks)
    {
        this.entityData.set(WOBBLE_TICKS, wobbleTicks);
    }

    public int getWobbleTicks()
    {
        return this.entityData.get(WOBBLE_TICKS);
    }

    public void setWobbleStrength(float strength)
    {
        this.entityData.set(WOBBLE_STRENGTH, strength);
    }

    public float getWobbleStrength()
    {
        return this.entityData.get(WOBBLE_STRENGTH);
    }

    public void invertWobbleDirection()
    {
        this.entityData.set(WOBBLE_DIRECTION, -entityData.get(WOBBLE_DIRECTION));
    }

    public EntityType<?> getHatchType()
    {
        return hatchType;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(net.minecraft.server.level.ServerEntity entity)
    {
        return new ClientboundAddEntityPacket(this);
    }

    public float getWobbleDirection()
    {
        return entityData.get(WOBBLE_DIRECTION);
    }
}
