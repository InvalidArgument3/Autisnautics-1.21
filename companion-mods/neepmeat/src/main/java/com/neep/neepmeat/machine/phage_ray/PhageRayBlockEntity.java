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
 *
 * NeepBus aim inputs are stored as the target pitch, yaw, and firing flag.
 */

package com.neep.neepmeat.machine.phage_ray;

import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.machine.EffectMachines;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PhageRayBlockEntity extends BlockEntity implements LivingMachineComponent {
    public static final float MIN_POWER = 0.1f;
    @Nullable
    private PhageRayEntity tethered;
    private float power;
    private float targetPitch;
    private float targetYaw;
    public boolean targetFiring;
    private int range = 8;

    public PhageRayBlockEntity(BlockPos pos, BlockState state) {
        super(EffectMachines.PHAGE_RAY_BE.get(), pos, state);
    }

    public void setPower(float power) {
        this.power = power;
    }

    public float getPower() {
        return power;
    }

    public boolean canRun() {
        return power >= MIN_POWER;
    }

    public void setAim(float pitch, float yaw, int range, boolean firing) {
        this.targetPitch = pitch;
        this.targetYaw = yaw;
        this.range = range;
        this.targetFiring = firing;
    }

    public void serverTick() {
        if (level == null) {
            return;
        }
        if (tethered == null || tethered.isRemoved()) {
            List<PhageRayEntity> found = level.getEntitiesOfClass(PhageRayEntity.class, new AABB(worldPosition));
            if (found.isEmpty()) {
                tethered = PhageRayEntity.create(level);
                tethered.setPos(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
                level.addFreshEntity(tethered);
            } else {
                tethered = found.get(0);
                for (int i = 1; i < found.size(); i++) {
                    found.get(i).remove(Entity.RemovalReason.DISCARDED);
                }
            }
        }
        tethered.setParent(this);
        tethered.setXRot(targetPitch);
        tethered.setYRot(targetYaw);
        tethered.setRange(range);
        tethered.externalTrigger = targetFiring;
    }

    @Nullable
    public PhageRayEntity getTetheredEntity() {
        return tethered;
    }

    @Override
    public boolean componentRemoved() {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType() {
        return LivingMachineComponents.PHAGE_RAY;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("target_pitch", targetPitch);
        tag.putFloat("target_yaw", targetYaw);
        tag.putBoolean("firing", targetFiring);
        tag.putInt("range", range);
        tag.putFloat("power", power);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        targetPitch = tag.getFloat("target_pitch");
        targetYaw = tag.getFloat("target_yaw");
        targetFiring = tag.getBoolean("firing");
        range = tag.getInt("range");
        power = tag.getFloat("power");
    }
}
