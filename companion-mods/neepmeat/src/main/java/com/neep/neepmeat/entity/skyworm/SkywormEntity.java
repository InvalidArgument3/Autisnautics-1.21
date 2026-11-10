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
 * Descend, hold, and ascend use the animation times. The altar spawn and particle burst are not in this class.
 */

package com.neep.neepmeat.entity.skyworm;

import com.neep.neepmeat.entity.NeepEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SkywormEntity extends Entity {
    private double startY;
    private double targetY;
    private int age;
    private SkywormAnimationTimes times = new SkywormAnimationTimes(1, 1, 1, 1);

    public SkywormEntity(EntityType<? extends SkywormEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public static SkywormEntity setup(Level world, Vec3 position) {
        SkywormEntity worm = new SkywormEntity(NeepEntities.SKYWORM.get(), world);
        worm.setPos(position.x, position.y, position.z);
        worm.startY = position.y;
        return worm;
    }

    public void begin(double targetY, SkywormAnimationTimes times) {
        this.targetY = targetY;
        this.times = times;
        this.startY = getY();
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        if (age <= times.descend()) {
            double progress = age / (double) Math.max(1, times.descend());
            setPos(getX(), startY + (targetY - startY) * progress, getZ());
        } else if (age > times.total()) {
            discard();
        }
        setDeltaMovement(Vec3.ZERO);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}
}
