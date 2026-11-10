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

package com.neep.neepmeat.machine.phage_ray;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepbus.block.entity.ConfigProvider;
import com.neep.neepbus.util.NeepBusConfig;
import com.neep.neepbus.util.SimpleEntry;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.init.NMEntities;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.transport.api.pipe.AbstractBloodAcceptor;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import com.neep.neepmeat.util.NMMaths;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class PhageRayBlockEntity extends SyncableBlockEntity implements LivingMachineComponent, ConfigProvider
{
    @Nullable private PhageRayEntity tetheredEntity;

    private float power;
    public final float minPower = 0.1f;

    private final NeepBusConfig config = NeepBusConfig.builder(this::setChanged)
            .input(new SimpleEntry("Pitch", "On data write:\nSet pitch (vertical rotation) to aim at."), d -> targetPitch = d.floatValue())
            .input(new SimpleEntry("Yaw", "On data write:\nSet yaw (horizontal rotation, bearing) to aim at."), d -> targetYaw = d.floatValue())
            .input(new SimpleEntry("Target pos", "On data write:\nAim towards the received position. Must be a string of the form @(X Y Z)"), d ->
            {
                var dest = d.wtValue();
                if (dest != null)
                {
                    var direction = dest.pos().getCenter().subtract(getBlockPos().getCenter());
                    var pol = NMMaths.rectToPol(direction.x(), direction.y(), direction.z());
                    targetPitch = pol.x;
                    targetYaw = pol.y;
                    setChanged();
                }
            })
            .input(new SimpleEntry("Fire", "On data write:\nSet beam firing state."), d -> targetFiring = d.boolValue())
            .applyChanges(this)
            .build();

    private float targetPitch = 0;
    private float targetYaw = 0;
    public boolean targetFiring = false;
    private int range;

    private final BloodAcceptor acceptor = new AbstractBloodAcceptor()
    {
        @Override
        public Mode getMode()
        {
            return Mode.ACTIVE_SINK;
        }

        @Override
        public float updateInflux(float influx)
        {
            if (influx >= minPower)
            {
                PhageRayBlockEntity.this.power = influx;
                return minPower;
            }
            PhageRayBlockEntity.this.power = 0;
            return 0;
        }
    };


    public PhageRayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public void serverTick()
    {
        if (tetheredEntity == null || tetheredEntity.isRemoved())
        {
            AABB box = new AABB(worldPosition.above());
            List<PhageRayEntity> entities = level.getEntitiesOfClass(PhageRayEntity.class, box, e -> true);

            if (entities.size() > 1)
            {
                NeepMeat.LOGGER.warn("Phage Ray at {} {} {} seems to have too many tethered entities!", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
            }

            if (entities.isEmpty())
            {
                tetheredEntity = NMEntities.PHAGE_RAY.create(level);

                if (!isBottom())
                    tetheredEntity.setPos(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
                else
                    tetheredEntity.setPos(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5 - tetheredEntity.getBbHeight(), worldPosition.getZ() + 0.5);
                level.addFreshEntity(tetheredEntity);
            }
            else
            {
                tetheredEntity = entities.get(0);
            }
        }

        if (level.getGameTime() % 5 == 0)
        {
            AABB box = new AABB(worldPosition.above());
            List<PhageRayEntity> entities = level.getEntitiesOfClass(PhageRayEntity.class, box, e -> true);

            if (entities.size() > 1)
            {
                NeepMeat.LOGGER.warn("Phage Ray at {} {} {} seems to have too many tethered entities!", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
                for (int i = 1; i < entities.size(); ++i)
                {
                    entities.get(i).remove(Entity.RemovalReason.DISCARDED);
                }
            }
        }

        if (tetheredEntity != null)
        {
            tetheredEntity.setParent(this);

            if (!tetheredEntity.hasExactlyOnePlayerPassenger())
            {
                tetheredEntity.setXRot(targetPitch);
                float limit = 95;
                if (targetPitch <= limit && targetPitch >= -limit)
                    tetheredEntity.setYRot(targetYaw);

                tetheredEntity.externalTrigger = targetFiring;
                tetheredEntity.setRange(range);
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putFloat("target_pitch", targetPitch);
        nbt.putFloat("target_yaw", targetYaw);
        nbt.putBoolean("firing", targetFiring);
        nbt.putInt("range", range);
        nbt.put("config", config.writeNbt(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.targetPitch = nbt.getFloat("target_pitch");
        this.targetYaw = nbt.getFloat("target_yaw");
        this.targetFiring = nbt.getBoolean("firing");
        this.range = nbt.getInt("range");
        this.config.readNbt(nbt.getCompound("config"));
    }

    public BloodAcceptor getBloodAcceptor()
    {
        return acceptor;
    }

    public float getPower()
    {
        return power;
    }

    public boolean canRun()
    {
        return power >= minPower;
    }

    @Override
    public boolean componentRemoved()
    {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType()
    {
        return LivingMachineComponents.PHAGE_RAY;
    }

    @Nullable
    public PhageRayEntity getTetheredEntity()
    {
        return tetheredEntity;
    }

    public NeepBusConfig getConfig()
    {
        return config;
    }

    public boolean isBottom()
    {
        return getBlockState().getValue(PhageRayBlock.BOTTOM);
    }

    public void setRange(int range)
    {
        this.range = Mth.clamp(range, 16, 128);
        setChanged();
    }

    public int getRange()
    {
        return range;
    }
}
